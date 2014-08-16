package com.example.procgen;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Control;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.paint.Color;

public class FXMLController implements Initializable {
    private IMapGenerator generator =
            new RandomMapGenerator(MainApp.X_CELLS, MainApp.Y_CELLS);

    @FXML
    private Canvas canvas;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("Generating map.");
        Map newMap = generator.generate();
        displayMap(newMap);
    }

    @FXML
    private void handleRandomMenuAction(ActionEvent event) {
        generator = new RandomMapGenerator(MainApp.X_CELLS, MainApp.Y_CELLS);
    }

    @FXML
    private void handleBinaryMenuAction(ActionEvent event) {
        // generator = new BinaryPartitionMapGenerator();
    }

    private void displayMap(Map map) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        ctx.setFill(Color.WHITE);
        for(int x = 0; x < map.getWidth(); x++) {
            for(int y = 0; y < map.getHeight(); y++) {
                if(map.getTile(x, y) == 1) {
                    ctx.fillRect(
                            x * MainApp.CELL_SIZE,
                            y * MainApp.CELL_SIZE,
                            MainApp.CELL_SIZE,
                            MainApp.CELL_SIZE);
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
