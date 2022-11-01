package youn.project.company.module.main;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import youn.project.company.module.file.FileService;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class MainController {

    private final FileService fileService;

    @RequestMapping(value="")
    public String index(Model model, @RequestPart(required = false) MultipartFile requestFile) throws IOException {
        if (requestFile != null && !requestFile.isEmpty()) {
            String fileName = fileService.requestFile(requestFile);
            model.addAttribute("targetResourceId", fileName);
        }

        return "index";
    }
}
