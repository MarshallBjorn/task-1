package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.TripPost;
import com.navrotskyi.trippyapi.domain.User;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class TripPostSeeder {

    public List<TripPost> getSampleTripPosts(List<TripNode> nodes, List<User> users) {
        List<TripPost> posts = new ArrayList<>();
        User jan = users.get(1);
        User anna = users.get(2);
        User marek = users.get(3);
        User zofia = users.get(4);
        User piotr = users.get(5);

        posts.add(createPost(nodes.get(0), jan, "Wyruszamy! Pociąg punktualnie."));
        posts.add(createPost(nodes.get(0), anna, "Nareszcie urlop!"));
        posts.add(createPost(nodes.get(3), anna, "Siedzimy w samolocie."));
        posts.add(createPost(nodes.get(4), marek, "Zaczynamy amerykański sen!"));
        posts.add(createPost(nodes.get(5), zofia, "Łódź odebrana."));
        posts.add(createPost(nodes.get(6), piotr, "Widok z Wieży Eiffla nocą to magia."));

        return posts;
    }

    private TripPost createPost(TripNode node, User reporter, String note) {
        TripPost post = new TripPost();
        post.setNode(node);
        post.setReporter(reporter);
        post.setNote(note);
        post.setPhotos(new ArrayList<>());
        return post;
    }
}