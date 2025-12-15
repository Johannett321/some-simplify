package com.somesimplify.somesimplify.rest;

import com.somesimplify.api.PostApi;
import com.somesimplify.model.GetSuggestedPublishDate200Response;
import com.somesimplify.model.PostStatus;
import com.somesimplify.model.PostTO;
import com.somesimplify.model.UpdatePostCommand;
import com.somesimplify.somesimplify.mapper.PostMapper;
import com.somesimplify.somesimplify.model.Post;
import com.somesimplify.somesimplify.service.PostService;
import com.somesimplify.somesimplify.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PostApiImpl implements PostApi {

    private final PostService postService;
    private final PostMapper postMapper;
    private final S3Service s3Service;

    @Override
    public ResponseEntity<List<PostTO>> getPosts(LocalDate fromDate, LocalDate toDate, PostStatus status) {
        List<Post> posts = postService.getPosts(fromDate, toDate, status);
        List<PostTO> postTOs = posts.stream()
                .map(this::mapPostWithUrls)
                .toList();
        return ResponseEntity.ok(postTOs);
    }

    @Override
    public ResponseEntity<PostTO> getPost(String id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(mapPostWithUrls(post));
    }

    private PostTO mapPostWithUrls(Post post) {
        PostTO postTO = postMapper.toPostTO(post);
        if (postTO.getContentFiles() != null) {
            postTO.getContentFiles().forEach(imageTO -> {
                if (imageTO.getId() != null) {
                    // Get the ContentFile from the post to get the S3 keys
                    post.getContentFiles().stream()
                            .filter(cf -> cf.getId().equals(imageTO.getId()))
                            .findFirst()
                            .ifPresent(cf -> {
                                imageTO.setUrl(s3Service.generatePresignedUrl(cf.getS3Key()));
                                imageTO.setThumbnailUrl(s3Service.generatePresignedUrl(cf.getThumbnailS3Key()));
                            });
                }
            });
        }
        return postTO;
    }

    @Override
    public ResponseEntity<PostTO> updatePost(String id, UpdatePostCommand updatePostCommand) {
        Post updatedPost = postService.updatePost(
                id,
                updatePostCommand.getText(),
                updatePostCommand.getPublishAt(),
                updatePostCommand.getStatus()
        );
        return ResponseEntity.ok(mapPostWithUrls(updatedPost));
    }

    @Override
    public ResponseEntity<GetSuggestedPublishDate200Response> getSuggestedPublishDate() {
        OffsetDateTime suggestedDate = postService.getSuggestedPublishDate();
        GetSuggestedPublishDate200Response response = new GetSuggestedPublishDate200Response();
        response.setSuggestedDate(suggestedDate);
        return ResponseEntity.ok(response);
    }
}
