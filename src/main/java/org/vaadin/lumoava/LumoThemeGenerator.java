package org.vaadin.lumoava;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

@Route("")
public class LumoThemeGenerator extends VerticalLayout {

    private final ChatClient chatClient;

    @Value("classpath:lumo-docs.txt")
    private Resource lumoDocs;

    public LumoThemeGenerator(ChatClient.Builder builder) {
        chatClient = builder.build();

        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);

        var markdown = new Markdown();
        var ui = UI.getCurrent();

        upload.addSucceededListener(e -> {
            var css = chatClient.prompt()
                .user(userMessage -> userMessage
                    .text("""
                        Use the uploaded image to generate a Lumo theme using the following documentation as a guideline:
                        
                        DOCUMENTATION:
                        ====
                        {documentation}
                        ====
                        Create a light and a dark variant.
                        Only use Lumo variables and Vaadin HTML elements, don't write custom CSS classes.
                        Output ONLY contents of the CSS file, nothing else.
                        """).param("documentation", lumoDocs)
                    .media(MimeTypeUtils.parseMimeType(e.getMIMEType()), new InputStreamResource(buffer.getInputStream())))
                .stream()
                .content()
                .subscribe(ui.accessLater(markdown::appendMarkdown, null));
            upload.clearFileList();
        });

        add(upload);
        addAndExpand(markdown);
    }


}
