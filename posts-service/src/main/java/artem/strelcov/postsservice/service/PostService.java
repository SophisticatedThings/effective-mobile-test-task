package artem.strelcov.postsservice.service;

import artem.strelcov.postsservice.dto.PostDto;
import artem.strelcov.postsservice.dto.PostRequest;
import artem.strelcov.postsservice.model.Post;
import artem.strelcov.postsservice.util.PostSort;
import artem.strelcov.postsservice.util.RestResponsePage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
public interface PostService {
    public List<Post> getAllPostsExceptMy(Principal user);
    /**
     * Возвращает все посты, которые создал пользователь с ником username
     */
    public List<PostDto> getPostsByUsername(String username);

    /**
     * Метод позволяет изменить title, content конкретного поста с параметром id.
     * Кроме того, метод позволяет вместе с этим добавить новые изображения в пост.
     */
    public Post updatePost(Integer id, Post post, MultipartFile[] images, Principal user);
    public void deletePost(Integer id, Principal user);
    public Post createPost(MultipartFile [] images,
                           Post post, Principal user);

    /**
     * Метод возвращает объект Page, который содержит в себе список постов каждого
     * пользователя, на которого подписан Principal, вызвавший данный метод
     * @param request - требуется для получения токена запроса, который используется
     *                в приватном вспомогательном методе getSubscriptionsOfUser
     * @param user - Principal, который вызвал метод и для которого нужно получить
     *                последние посты от тех, на кого он подписан
     * @param offset - номер страницы, которую нужно вывести
     * @param limit - количество постов, которые можно вывести на одной странице
     * @param sort - тип сортировки постов по времени создания. Принимает одно из следующих
     *                значений: DATE_ASC - показать сначала старые посты
     *                          DATE_DESC - показать сначала новые посты
     *                          отсутствие этого request_param - нет сортировки
     * @return - возвращает обертку над Page, и представляет из себя список постов и
     *              параметры пагинации, которые можно будет обрабатывать на фронте
     */
    public RestResponsePage<PostDto> getPostsBySubscriptionsWithPagination(
            HttpServletRequest request,
            Principal user,
            Integer offset,
            Integer limit,
            PostSort sort);
}
