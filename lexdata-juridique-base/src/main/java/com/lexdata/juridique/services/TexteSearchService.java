package com.lexdata.juridique.services;

import com.lexdata.juridique.dto.SearchResultDto;
import com.lexdata.juridique.models.TexteJuridique;
import com.lexdata.juridique.models.TexteJuridiqueDocument;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.lexdata.juridique.repository.TexteJuridiqueSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TexteSearchService {

    private final TexteJuridiqueSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public void indexTexte(TexteJuridique texte) {
        TexteJuridiqueDocument doc = TexteJuridiqueDocument.builder()
                .id(texte.getId().toString())
                .titre(texte.getTitre())
                .referenceOfficielle(texte.getReferenceOfficielle())
                .type(texte.getType().name())
                .domaine(texte.getDomaine().name())
                .dateSignature(texte.getDateSignature())
                .contenu(texte.getContenu())
                .estPublie(texte.getEstPublie())
                .estPremium(texte.getEstPremium())
                .build();
        searchRepository.save(doc);
        log.info("Texte indexé dans Elasticsearch: {}", texte.getId());
    }

    public void removeTexte(Long id) {
        searchRepository.deleteById(id.toString());
        log.info("Texte supprimé d'Elasticsearch: {}", id);
    }

    public List<SearchResultDto> search(String query, boolean includeNonPublished) {
        // Construction de la requête avec recherche sur titre et contenu + flou
        // (fuzziness)
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(m -> m.multiMatch(mm -> mm
                                .fields("titre", "contenu")
                                .query(query)
                                .fuzziness("AUTO")
                                .operator(Operator.And)))
                        .filter(f -> includeNonPublished
                                ? f.matchAll(ma -> ma)
                                : f.term(t -> t.field("estPublie").value(true)))))
                .withHighlightQuery(
                        new HighlightQuery(
                                new Highlight(List.of(new HighlightField("contenu"))),
                                String.class))
                .build();

        SearchHits<TexteJuridiqueDocument> hits = elasticsearchOperations.search(nativeQuery,
                TexteJuridiqueDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> {
                    TexteJuridiqueDocument doc = hit.getContent();
                    List<String> highlights = hit.getHighlightField("contenu");

                    return SearchResultDto.builder()
                            .id(Long.parseLong(doc.getId()))
                            .titre(doc.getTitre())
                            .referenceOfficielle(doc.getReferenceOfficielle())
                            .domaine(doc.getDomaine())
                            .type(doc.getType())
                            .highlights(highlights)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
