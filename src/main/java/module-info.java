module qpm {
    requires qilletni.api;
    requires com.google.gson;
    requires info.picocli;
    requires org.slf4j;
    requires jdk.httpserver;
    requires okhttp3;

    // Allows Picocli annotation processing
    opens max.burdett.qpm to info.picocli;
}

