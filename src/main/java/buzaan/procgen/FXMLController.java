package buzaan.procgen;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class FXMLController implements Initializable {
    private final Logger log = Logger.getLogger(FXMLController.class.getName());
    private IMapGenerator generator =
            //new RandomMapGenerator(MainApp.X_CELLS, MainApp.Y_CELLS);
            //new BinaryPartitionMapGenerator(MainApp.X_CELLS, MainApp.Y_CELLS);
            new CellularAutomataMapGenerator(MainApp.X_CELLS, MainApp.Y_CELLS);
    private final Executor executor = Executors.newFixedThreadPool(
            1, new DaemonThreadFactory());

    private class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread out = new Thread(r);
            out.setDaemon(true);
            return out;
        }
    }

    @FXML
    private Canvas canvas;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        final Task<Map> task = new Task<Map>() {
            @Override
            protected Map call() throws Exception {
                Thread.currentThread().setName("Generator");
                return generator.generate();
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    Map map = task.get();
                    displayMap(map);
                    log.log(Level.INFO, "Generation successful.");
                } catch(InterruptedException ex) {
                    log.log(Level.SEVERE, "Generation interrupted", ex);
                } catch (ExecutionException ex) {
                    log.log(Level.SEVERE, "Error generating level", ex);
                }
            }
        });
        executor.execute(task);
    }

    @FXML
    private void handleRandomMenuAction(ActionEvent event) {
        generator = new RandomMapGenerator(MainApp.X_CELLS, MainApp.Y_CELLS);
    }

    @FXML
    private void handleBinaryMenuAction(ActionEvent event) {
        generator = new BinaryPartitionMapGenerator(MainApp.X_CELLS, MainApp.Y_CELLS);
    }

    @FXML
    private void handleCAMenuAction(ActionEvent event) {
        generator = new CellularAutomataMapGenerator(MainApp.X_CELLS, MainApp.Y_CELLS);
    }

    @FXML
    private void handleBilinearTerrainAction(ActionEvent event) {
        generator = new InterpolatedTerrainGenerator(
                MainApp.X_CELLS,
                MainApp.Y_CELLS,
                InterpolatedTerrainGenerator.Bilinear(8));
    }

    @FXML
    private void handleBicubicTerrainAction(ActionEvent event) {
        generator = new InterpolatedTerrainGenerator(
                MainApp.X_CELLS,
                MainApp.Y_CELLS,
                InterpolatedTerrainGenerator.Bicubic(8));
    }

    @FXML
    private void handleGradientAction(ActionEvent event) {
        generator = new InterpolatedTerrainGenerator(
                MainApp.X_CELLS,
                MainApp.Y_CELLS,
                InterpolatedTerrainGenerator.Gradient(8));
    }

    @FXML
    private void handleFractalAction(ActionEvent event) {
        generator = new FractalTerrainGenerator(
                MainApp.X_CELLS, MainApp.Y_CELLS, 8);
    }

    private void displayMap(Map map) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        ctx.setFill(Color.WHITE);
        for(int x = 0; x < map.getWidth(); x++) {
            for(int y = 0; y < map.getHeight(); y++) {
                int cval = map.getTile(x, y);
                ctx.setFill(Color.rgb(
                        (cval & 0xff0000) >> 16,
                        (cval & 0x00ff00) >> 8,
                        (cval & 0x0000ff)));
                ctx.fillRect(
                        x * MainApp.CELL_SIZE,
                        y * MainApp.CELL_SIZE,
                        MainApp.CELL_SIZE,
                        MainApp.CELL_SIZE);
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
