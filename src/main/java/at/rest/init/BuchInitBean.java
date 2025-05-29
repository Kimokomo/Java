package at.rest.init;


import at.rest.dtos.BuchDTO;
import at.rest.mapper.BuchMapper;
import at.rest.model.Buch;
import at.rest.repositories.BuchRepository;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
@Startup
public class BuchInitBean {

    @Inject
    BuchRepository buchRepository;

    @Inject
    private BuchMapper buchMapper;

    @PostConstruct
    public void init() {

        List<BuchDTO> buecher = List.of(
                BuchDTO.builder().titel("Der Alchimist").autor("Paulo Coelho").erscheinungsjahr(1988).verfuegbar("J").build(),
                BuchDTO.builder().titel("Schatten des Windes").autor("Carlos Ruiz Zafón").erscheinungsjahr(2001).verfuegbar("J").build(),
                BuchDTO.builder().titel("1984").autor("George Orwell").erscheinungsjahr(1949).verfuegbar("N").build(),
                BuchDTO.builder().titel("Die Entdeckung der Langsamkeit").autor("Sten Nadolny").erscheinungsjahr(1983).verfuegbar("J").build(),
                BuchDTO.builder().titel("Momo").autor("Michael Ende").erscheinungsjahr(1973).verfuegbar("J").build(),
                BuchDTO.builder().titel("Der Steppenwolf").autor("Hermann Hesse").erscheinungsjahr(1927).verfuegbar("N").build(),
                BuchDTO.builder().titel("Der Name der Rose").autor("Umberto Eco").erscheinungsjahr(1980).verfuegbar("J").build(),
                BuchDTO.builder().titel("Die Verwandlung").autor("Franz Kafka").erscheinungsjahr(1915).verfuegbar("J").build(),
                BuchDTO.builder().titel("Faust").autor("Johann Wolfgang von Goethe").erscheinungsjahr(1808).verfuegbar("N").build(),
                BuchDTO.builder().titel("Der kleine Prinz").autor("Antoine de Saint-Exupéry").erscheinungsjahr(1943).verfuegbar("J").build(),

                BuchDTO.builder().titel("Die Säulen der Erde").autor("Ken Follett").erscheinungsjahr(1989).verfuegbar("J").build(),
                BuchDTO.builder().titel("Der Herr der Ringe").autor("J.R.R. Tolkien").erscheinungsjahr(1954).verfuegbar("N").build(),
                BuchDTO.builder().titel("Harry Potter und der Stein der Weisen").autor("J.K. Rowling").erscheinungsjahr(1997).verfuegbar("J").build(),
                BuchDTO.builder().titel("Stolz und Vorurteil").autor("Jane Austen").erscheinungsjahr(1813).verfuegbar("J").build(),
                BuchDTO.builder().titel("Die Tribute von Panem").autor("Suzanne Collins").erscheinungsjahr(2008).verfuegbar("J").build(),
                BuchDTO.builder().titel("Der große Gatsby").autor("F. Scott Fitzgerald").erscheinungsjahr(1925).verfuegbar("N").build(),
                BuchDTO.builder().titel("To Kill a Mockingbird").autor("Harper Lee").erscheinungsjahr(1960).verfuegbar("J").build(),
                BuchDTO.builder().titel("Die unendliche Geschichte").autor("Michael Ende").erscheinungsjahr(1979).verfuegbar("J").build(),
                BuchDTO.builder().titel("Der Fänger im Roggen").autor("J.D. Salinger").erscheinungsjahr(1951).verfuegbar("N").build(),
                BuchDTO.builder().titel("Die Göttliche Komödie").autor("Dante Alighieri").erscheinungsjahr(1320).verfuegbar("J").build()
        );

        List<Buch> buchEntities = buecher.stream()
                .map(buchMapper::toEntity)
                .toList();

        buchRepository.saveAll(buchEntities);

    }
}
