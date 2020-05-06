package scot.alba.webpostits;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationStartUp implements ServletContextListener {

    private static final Logger log = Logger.getLogger(ApplicationStartUp.class.getName());

    private static final String QIQOCHAT_URL = "https://jalba.qiqochat.com";

    public static final String QIQO_CHAT_PAGE = "https://qiqochat.com/c/pmtyyRCS/";

    public static final String TOPIC_REPOSITORY = "topic_repository";

    private ServletContext context;

    // In a servlet plain Threads should not be used
    // The prefered approach in Java EE 7 is to use ScheduledExecutorService
    // Does away with the Thread.sleep
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {

        scheduler = Executors.newSingleThreadScheduledExecutor();

        context = contextEvent.getServletContext();
        context.setAttribute(TOPIC_REPOSITORY, new ArrayList<Map<String, Object>>());

        // 5 second interval
        scheduler.scheduleAtFixedRate(new Scraper(), 0, 5, TimeUnit.SECONDS);
    }

    private void scrapeQiqochat() {
        try {
            List<Map<String, Object>> topicsDto = (List<Map<String, Object>>) context.getAttribute(TOPIC_REPOSITORY);
            int topicCount = determineNextTopicId(topicsDto);
            Topics topics = new Topics(topicsDto);

            Document doc = Jsoup.connect(QIQO_CHAT_PAGE).get();
            Elements elementsByClass = doc.getElementsByClass("conversation-starter-label");

            for (Element topic : elementsByClass) {
                Topic t = createTopic(topicCount++, topic);
                topics.addOrUpdate(t);
            }
            topicsDto.clear();
            topicsDto.addAll(topics.get());
        } catch (IOException ex) {
            log.log(Level.WARNING,"problem retrieving scraped data", ex);
        }
    }

    private int determineNextTopicId(List<Map<String, Object>> topicsDto) {
        if (topicsDto.isEmpty()) {
            return 1;
        }
        return topicsDto.size() + 1;
    }

    private Topic createTopic(int topicCount, Element topic) {
        TextNode topicTitle = ((TextNode) topic.childNodes().get(0));

        Elements topicText = topic.parent().getElementsByTag("p");
        String desc = ((TextNode) topicText.get(0).childNodes().get(0)).getWholeText();

        Element authorImageUrl = topic.siblingElements().get(0).children().get(0);
        String authorName = authorImageUrl.attributes().get("alt");

        String authorImg = authorImageUrl.attributes().get("src");
        if (authorImg.startsWith("/")) {
            authorImg = QIQOCHAT_URL + authorImg;
        }

        return new Topic(topicCount, topicTitle.getWholeText(), desc, authorName, authorImg);
    }

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        scheduler.shutdown();

    }

    // 
    /**
     * Inner class to run the Scheduled service
     * This allow access to scrapeQiqochat() in the enclosing class
     */
    class Scraper implements Runnable {

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            log.info("start scraping...");
            scrapeQiqochat();
            time = System.currentTimeMillis() - time;
            log.log(Level.INFO, "scraped in {0}ms", time);
        }
    }
}
