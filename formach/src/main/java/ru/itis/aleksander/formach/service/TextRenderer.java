package ru.itis.aleksander.formach.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itis.aleksander.formach.repository.UserRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("textRenderer")
@RequiredArgsConstructor
public class TextRenderer {

    private static final Pattern MENTION = Pattern.compile("@([a-zA-Z0-9_]{3,20})");

    private static final Pattern URL = Pattern.compile(
            "https?://[\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]+");

    private static final Pattern YOUTUBE_HREF = Pattern.compile(
            "<a [^>]*href=\"https?://(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]{6,15})[^\"]*\"[^>]*>[^<]*</a>");

    private final UserRepository userRepository;

    public String render(String input) {
        if (input == null || input.isEmpty()) return "";
        String html = autolinkAndEscape(input);
        html = replaceMentions(html);
        html = embedYouTube(html);
        return html;
    }


    private String autolinkAndEscape(String s) {
        Matcher m = URL.matcher(s);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (m.find()) {
            out.append(escapeHtml(s.substring(last, m.start())));
            String url = m.group();
            out.append("<a href=\"").append(url)
               .append("\" target=\"_blank\" rel=\"noopener\">")
               .append(escapeHtml(url))
               .append("</a>");
            last = m.end();
        }
        out.append(escapeHtml(s.substring(last)));
        return out.toString();
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br>\n");
    }

    private String replaceMentions(String s) {
        Matcher m = MENTION.matcher(s);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String login = m.group(1);
            String replacement = userRepository.findByLogin(login)
                    .map(u -> "<a href=\"/users/" + u.getId()
                            + "\" class=\"mention\">@" + login + "</a>")
                    .orElse("@" + login);
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString();
    }

    private String embedYouTube(String s) {
        Matcher m = YOUTUBE_HREF.matcher(s);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String id = m.group(1);
            String iframe = "<div class=\"yt-embed ratio ratio-16x9 my-2\" style=\"max-width:560px\">" +
                    "<iframe src=\"https://www.youtube.com/embed/" + id + "\" " +
                    "title=\"YouTube\" frameborder=\"0\" allowfullscreen></iframe></div>";
            m.appendReplacement(out, Matcher.quoteReplacement(iframe));
        }
        m.appendTail(out);
        return out.toString();
    }
}
