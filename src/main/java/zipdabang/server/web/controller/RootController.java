package zipdabang.server.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "루트 API", description = "root")
public class RootController {

    @GetMapping("/health")
    public String healthAPi(){
        return "i'm healthy";
    }
}
