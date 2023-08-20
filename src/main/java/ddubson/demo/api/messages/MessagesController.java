package ddubson.demo.api.messages;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessagesController {
    @GetMapping("/api/messages")
    @PreAuthorize("SCOPE_message.read")
    public List<String> messages() {
        return List.of("M1", "M2", "M3");
    }
}
