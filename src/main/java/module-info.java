module qpm {
    requires qilletni.api;
    requires com.google.gson;

    // Allows Picocli annotation processing
    opens com.example.myapp to info.picocli;
}

