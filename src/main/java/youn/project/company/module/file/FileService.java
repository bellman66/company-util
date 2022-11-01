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
import youn.project.company.setting.props.ApiProps;
import youn.project.company.setting.props.FileProps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final ApiProps apiProps;
    private final FileProps fileProps;
    private final RestTemplate restTemplate;

    public String requestFile(MultipartFile requestFile) throws IOException {
        String fileName = "";
        String resourceUrl = "";
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = requestFile.getInputStream();

            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            // 1. 모든 row id 취합
            List<String> bNoList = new ArrayList<>();
            for(int rowIdx = sheet.getFirstRowNum(); rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                XSSFRow row = sheet.getRow(rowIdx);
                XSSFCell cell = row.getCell(0);
                bNoList.add(cell.getRawValue());
            }

            // 2. request API
            List<String> taxTypeList = new ArrayList<>();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.put("b_no", bNoList);

            // 중요 : URI 가 인코딩되는과정에서 %가 아스키 25번으로 중복으로 더해짐 그래서 이렇게써줘야함
            URI uri = URI.create(apiProps.getApiUrl() + "?serviceKey=" + apiProps.getApiKey());
            HttpEntity<?> httpEntity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(uri, httpEntity, JsonNode.class);
            if(response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = response.getBody();
                XSSFRow newRow = sheet.createRow(1);

                for (JsonNode data : result.withArray("data")) {
                    String bNo = data.path("b_no").textValue();             // 사업자번호
                    String taxType = data.path("tax_type").textValue();      // Value

                    taxTypeList.add(taxType);
                }
            }

            // 3. Set Tax Type value
            if (bNoList.size() == taxTypeList.size()) {
                for(int idx= 0; idx<taxTypeList.size(); idx++) {
                    XSSFRow row = sheet.getRow(idx);
                    XSSFCell cell = row.createCell(1);
                    cell.setCellValue(taxTypeList.get(idx));
                }
            }

            fileName = UUID.randomUUID().toString();
            resourceUrl = fileProps.getBaseUrl() + fileName + ".xlsx";
            File file = new File(resourceUrl);

            outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            inputStream.close();
            outputStream.close();
        }

        return fileName;
    }
}
