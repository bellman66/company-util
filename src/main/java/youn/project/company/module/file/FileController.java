package youn.project.company.module.file;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import youn.project.company.setting.props.FileProps;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/file")
public class FileController {

    private final FileProps fileProps;

    @GetMapping(value = "/download")
    public void downloadFile(@RequestParam(value = "idx", required = false) String idx ,
                             HttpServletResponse response) throws IOException {
        if (idx == null) throw new RuntimeException("올바르지 않은 접근입니다.");

        String fileName = idx + ".xlsx";
        String resourceUrl = fileProps.getBaseUrl() + fileName;
        File file = new File(resourceUrl);
        if (!file.exists()) throw new RuntimeException("파일이 존재하지않음.");

        try {
            byte[] responseData = FileUtils.readFileToByteArray(file);

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLength(responseData.length);
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(fileName, "UTF-8") + "\";");

            response.getOutputStream().write(responseData);
            response.getOutputStream().flush();
            FileUtils.forceDelete(file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            response.getOutputStream().close();
        }
    }

}
