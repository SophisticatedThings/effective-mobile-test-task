package artem.strelcov.postsservice.util;

import artem.strelcov.postsservice.model.ImageModel;

import java.util.List;

public class MapToStringUrls {
    public static List<String> map(List<ImageModel> postImages) {
        return postImages.stream()
                .map(ImageModel::getImageUrl)
                .toList();
    }
}
