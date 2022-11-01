package youn.project.company.setting.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app.configuration.api")
public class ApiProps {
    private String apiUrl;
    private String apiKey;
}
