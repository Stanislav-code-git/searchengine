package searchengine.config;

import lombok.Getter;
import lombok.Setter;

import java.net.URL;

@Getter
@Setter
public class Site {
    private URL url;
    private String name;
}