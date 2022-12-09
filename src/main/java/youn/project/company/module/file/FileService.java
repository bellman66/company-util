package youn.project.company.module.file;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import youn.project.company.module.file.data.PortalRes;
import youn.project.company.setting.props.ApiProps;
import youn.project.company.setting.props.FileProps;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FileService {

    private final ApiProps apiProps;
    private final FileProps fileProps;
    private final RestTemplate restTemplate;

    public String requestFile(MultipartFile requestFile) {
        String fileName = "";
        String resourceUrl = "";

        fileName = UUID.randomUUID().toString();
        resourceUrl = fileProps.getBaseUrl() + fileName + ".xlsx";
        File file = new File(resourceUrl);

        try (
                InputStream inputStream = requestFile.getInputStream();
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                FileOutputStream outputStream = new FileOutputStream(file)
        ) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            List<String> bNoList = new ArrayList<>();
            Queue<PortalRes> portalResQueue = new LinkedList<>();

            int currentIdx = sheet.getFirstRowNum();
            int endRowNum = sheet.getLastRowNum();

            int defaultStepCnt = 100;
            while (currentIdx < endRowNum) {
                int stepCnt = currentIdx + defaultStepCnt <= endRowNum ?
                        defaultStepCnt : endRowNum - currentIdx;

                // Insert Target bNo
                IntStream.range(currentIdx, currentIdx + stepCnt)
                        .mapToObj(rowIdx -> {
                            XSSFRow row = sheet.getRow(rowIdx);
                            XSSFCell cell = row.getCell(0);
                            return cell.getRawValue();
                        })
                        .forEach(bNoList::add);

                // Call Data Portal
                ResponseEntity<JsonNode> portalData = callDataportal(bNoList);

                // Guard - Not 200 Receive
                if (portalData.getStatusCode() != HttpStatus.OK) break;

                assert portalData.getBody() != null;
                for (JsonNode data : portalData.getBody().withArray("data")) {
                    String bStt = data.path("b_stt").textValue();             // 사업자번호
                    String taxType = data.path("tax_type").textValue();
                    String endDt = data.path("end_dt").textValue();    // Value

                    portalResQueue.add(PortalRes.create(bStt, taxType, endDt));
                }

                for (int rowIdx = currentIdx; rowIdx < currentIdx + stepCnt; rowIdx++) {
                    // Create Cell
                    XSSFRow row = sheet.getRow(rowIdx);
                    XSSFCell bSttCell = row.createCell(1);
                    XSSFCell taxTypeCell = row.createCell(2);
                    XSSFCell endDtCell = row.createCell(3);

                    // Target Value
                    PortalRes target = portalResQueue.poll();

                    bSttCell.setCellValue(target.getBStt());
                    taxTypeCell.setCellValue(target.getTaxType());
                    endDtCell.setCellValue(target.getEndDt());
                }

                bNoList.clear();
                portalResQueue.clear();

                currentIdx += defaultStepCnt;
            }

            workbook.write(outputStream);
            outputStream.flush();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return fileName;
    }

    private ResponseEntity<JsonNode> callDataportal(List<String> aBNoList) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.put("b_no", aBNoList);

        // 중요 : URI 가 인코딩되는과정에서 %가 아스키 25번으로 중복으로 더해짐 그래서 이렇게써줘야함
        URI uri = URI.create(apiProps.getApiUrl() + "?serviceKey=" + apiProps.getApiKey());
        HttpEntity<?> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity(uri, httpEntity, JsonNode.class);
    }
}
