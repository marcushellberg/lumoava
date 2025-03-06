package org.vaadin.lumoava;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;

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
            chatClient.prompt()
                .user(userMessage -> userMessage
                    .text("""
                        Use the uploaded image to generate a Lumo theme using the following documentation as a guideline:
                        
                        DOCUMENTATION:
                        ====
                        {documentation}
                        ====
                        Create a light and a dark variant.
                        Only use Lumo variables and Vaadin HTML element selectors, don't write custom CSS classes or properties.
                        Pay special attention to input fields and buttons. Use an appropriate border radius, width, and color.
                        Use the common `--vaadin-input-field` prefixed properties to update all input fields in a coherent manner.                      
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
