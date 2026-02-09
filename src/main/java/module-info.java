module spring.shield {
    // Moduli JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Moduli Spring
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;

    // Altre utility
    requires org.slf4j;
    requires static lombok;
    requires jakarta.annotation;
    requires java.sql;

    // Apre il pacchetto alla riflessione di Spring e JavaFX
    opens com.cryptoproject.spring_shield to javafx.fxml, spring.core, spring.context, spring.beans;

    // Esporta il pacchetto
    exports com.cryptoproject.spring_shield;
}