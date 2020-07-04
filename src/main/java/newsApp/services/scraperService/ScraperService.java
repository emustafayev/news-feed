package newsApp.services.scraperService;


import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import newsApp.models.newsModel.News;
import newsApp.models.scraperModel.DocumentAndSkeleton;
import newsApp.models.scraperModel.ScraperSkeleton;
import newsApp.utils.webScraperUtils.NewsSitesSkeleton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
public class ScraperService {
    final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML," +
            "like Gecko) Chrome/51.0.2704.103 Safari/537.36";

    /**
     * generic helper functions
     *
     */
    private <T> List<T> toList(Set<T> keySet) {
        return new ArrayList<>(keySet);
    }

    /**
     * generic main functions
     *
     */

    private <S,R> List<R> fold(List<S> data , Function<S,R> f){
        List<R> result = new ArrayList<>();
        data.stream().parallel().forEach(element->{
            try {
                result.add(f.apply(element));
            }catch (Exception ignored){
                log.error("Get Document exception");
            }
        });
        return result;
    }

    /**
     *  parent function
     */
     public List<News> scrap(){

         // News Skeleton Data
         List<ScraperSkeleton> scraperSkeletons = NewsSitesSkeleton.get();


         Function<ScraperSkeleton, DocumentAndSkeleton> f = new Function<ScraperSkeleton, DocumentAndSkeleton>() {
             @SneakyThrows
             @Override
             public DocumentAndSkeleton apply(ScraperSkeleton skeleton) {
                 return new DocumentAndSkeleton(Jsoup.connect(skeleton.getWebSite()).userAgent(USER_AGENT).get(),skeleton);
             }
         };

         List<DocumentAndSkeleton> folded = fold(
                 scraperSkeletons,
                 f
         );
         return getNews(folded);

     }

    private List<News> getNews(List<DocumentAndSkeleton> folded) { // list of news; link=>content; Map<String, DetailedNews> data???
        return folded.stream().flatMap(o ->
                StreamOfSection(o).flatMap(sec ->
                        StreamOfContent(o, sec).map(content -> {
                            try {
                                String title = content.select(o.getSkeleton().getPathToTitle()).text();  // text?? look back, add attr name too;
                                String rowImg = content.select(o.getSkeleton().getPathToImg()).attr(o.getSkeleton().getImageAttr());
                                String image = rowImg.startsWith("http") ? rowImg : o.getSkeleton().getWebSite().substring(0,o.getSkeleton().getWebSite().length()-1).concat(rowImg);
                                log.info("image: "+content.select(o.getSkeleton().getPathToImg()).html());
                                String newsLink = content.select(o.getSkeleton().getPathToNewsLink()).attr("href");
                                return new News(UUID.randomUUID(), title, newsLink, o.getSkeleton().getWebSite(), image, LocalDateTime.now());
                            } catch (Exception ignored) {
                                log.error("Site content exception!");
                            }
                            throw new IllegalArgumentException("Not finished");  //FIXME :: not a correct structure
                        }))
        )
//                .filter(News::isNull)
                .collect(Collectors.toList());// not sure about data type? might be HashMap; !!!!!!!!!!!!!!!
     }



    private Stream<Element> StreamOfSection(DocumentAndSkeleton documentAndSkeleton){
         return documentAndSkeleton.getDocument().select(documentAndSkeleton.getSkeleton().getPathToSection()).stream();
    }

    private Stream<Element> StreamOfContent(DocumentAndSkeleton documentAndSkeleton, Element element){
         return element.select(documentAndSkeleton.getSkeleton().getPathToContent()).stream();
    }



}
