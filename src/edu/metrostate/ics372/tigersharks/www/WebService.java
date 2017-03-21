package edu.metrostate.ics372.tigersharks.www;

import edu.metrostate.ics372.tigersharks.Library;
import edu.metrostate.ics372.tigersharks.LibraryItem;
import edu.metrostate.ics372.tigersharks.Servicable;
import edu.metrostate.ics372.tigersharks.io.database.LibraryItemDatabase;
import edu.metrostate.ics372.tigersharks.io.Store;
import edu.metrostate.ics372.tigersharks.www.http.get.Item;
import edu.metrostate.ics372.tigersharks.www.http.get.Items;
import edu.metrostate.ics372.tigersharks.www.http.get.Upload;
import spark.Spark;

import javax.servlet.MultipartConfigElement;

import java.io.InputStream;
import java.util.Optional;

import static spark.Spark.*;

/**
 * Created by sleig on 3/15/2017.
 */
public class WebService {
    private static final String QUERY_PARAM_ID = "i";
    private static final String QUERY_PARAM_NAME = "n";
    private static final String QUERY_PARAM_TYPE = "t";
    private static final String QUERY_PARAM_META = "m";
    private static final String ENDPOINT_ITEM = "/item";
    private static final String ENDPOINT_ITEMS = "/items";
    private static final String ENDPOINT_UPLOAD = "/upload";

    private final Servicable<LibraryItem> loanableService;

    private WebService(Servicable<LibraryItem> loanableService) {
        this.loanableService = loanableService;
    }

    public void start() {
        get(ENDPOINT_ITEM, (req, res) -> {
            final String itemId = req.queryParams(QUERY_PARAM_ID);
            Optional<LibraryItem> libraryItemOptional = loanableService.read(loanable -> loanable.getId().equals(itemId));
            if(libraryItemOptional.isPresent()) {
                return new Item(libraryItemOptional.get()).render();
            }
            return null;
        });
        get(ENDPOINT_ITEMS, (req, res) -> new Items(loanableService.readAll()).render());
        get(ENDPOINT_UPLOAD, (req, res) -> new Upload());
        post(ENDPOINT_ITEM, (req,res) -> {
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            try (InputStream is = req.raw().getPart("uploaded_file").getInputStream()) {
                // Use the input stream to create a file
            }
            return "File uploaded";
        });
    }

    public void stop() {
        Spark.stop();
    }

    public static void main(String[] args) {
        new WebService(new Library(new Store(LibraryItemDatabase.getInstance()))).start();
    }
}

