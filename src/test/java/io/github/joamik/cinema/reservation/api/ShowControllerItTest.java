package io.github.joamik.cinema.reservation.api;

import akka.actor.typed.ActorSystem;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.reactive.server.WebTestClient;

import static io.github.joamik.cinema.reservation.domain.ShowFixture.randomShowId;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class ShowControllerItTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private ActorSystem<Void> actorSystem;

    @AfterEach
    void tearDown() {
        actorSystem.terminate();
    }

    @Test
    void shouldGetShowById() {
        // given
        var showId = randomShowId().id().toString();

        // when // then
        webClient.get().uri("/shows/{showId}", showId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ShowResponse.class).value(new BaseMatcher<>() {
                    @Override
                    public boolean matches(Object actual) {
                        if (actual instanceof ShowResponse showResponse) {
                            return showResponse.id().equals(showId);
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendValue("ShowResponse should have id: " + showId);
                    }
                });
    }
}