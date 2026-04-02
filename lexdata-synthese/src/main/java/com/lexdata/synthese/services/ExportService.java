package com.lexdata.synthese.services;

import com.lexdata.synthese.models.FicheSynthetique;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class ExportService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public ExportService() {
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder().build();
    }

    public byte[] generateFichePdf(FicheSynthetique fiche) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            String htmlContent = buildHtmlContent(fiche);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new RuntimeException("Échec de la génération du PDF", e);
        }
    }

    private String buildHtmlContent(FicheSynthetique fiche) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
                .append("body { font-family: 'Arial', sans-serif; padding: 40px; color: #333; }")
                .append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }")
                .append("h2 { color: #2980b9; margin-top: 25px; }")
                .append(".meta { color: #7f8c8d; font-size: 0.9em; margin-bottom: 30px; }")
                .append(".section { margin-bottom: 20px; text-align: justify; line-height: 1.6; }")
                .append("</style></head><body>");

        html.append("<h1>").append(fiche.getTitre()).append("</h1>");
        html.append("<div class='meta'>Généré le: ").append(java.time.LocalDateTime.now()).append("</div>");

        appendSection(html, "Objectif Principal", fiche.getObjectifPrincipal());
        appendSection(html, "Changements Clés", fiche.getChangementsCles());
        appendSection(html, "Obligations", fiche.getObligations());
        appendSection(html, "Sanctions", fiche.getSanctions());
        appendSection(html, "Conseils Pratiques", fiche.getConseilsPratiques());
        appendSection(html, "Exemples Concrets", fiche.getExemplesConcrets());

        html.append("</body></html>");
        return html.toString();
    }

    private void appendSection(StringBuilder html, String title, String markdown) {
        if (markdown != null && !markdown.isBlank()) {
            html.append("<h2>").append(title).append("</h2>");
            html.append("<div class='section'>").append(markdownToHtml(markdown)).append("</div>");
        }
    }

    private String markdownToHtml(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
