package truss;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.event.ActionEvent;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Hello world!
 *
 */
public class App extends Application
{
    //variables that start with p represent things in pixels
    //variables that start with u represent things in units the user sees
    static double ratio = 1;
    static double pWidth = 0, pHeight = 0;
    static double uWidth = 0, uHeight = 0;
    static double pStep = 0;
    static double uStep = 0;
    static double pOffsetX = 0, pOffsetY = 0;

    static Stage primaryStage;
    static Scene scene;
    static Group group;
    static GraphicsContext gc;
    static VBox vbox = new VBox();
    
    static DecimalFormat df = new DecimalFormat("#");
    
    static final int NODENAME = 0, NODEX = 1, NODEY = 2, SUPPORTNAME = 3, SUPPORTX = 4, SUPPORTY = 5, 
                     BEAMNODEA = 6, BEAMNODEB = 7, FORCENODE = 8, FORCEVAL = 9, FORCEDIR = 10;
    static TextField[] textfields = new TextField[11];

    static final int ADDNODE = 0, ADDSUPP = 1, ADDBEAM = 2, ADDFORCE = 3, OPTIONS = 4, CALC = 5;
    static Button[] buttons = new Button[6];

    static final int ROLLER = 0, PIN = 1, FIXED = 2, NOTSUPP = 3;
    static RadioButton[] rdButtons = new RadioButton[3];

    static boolean visibleGrid = true;
    static boolean visibleMinorGrid = true;
    static boolean toggled = true;

    static ArrayList<Joint> nodes = new ArrayList<Joint>(0);
    static Map<String, Joint> nodeMap = new HashMap<String, Joint>(0);
    static ArrayList<Beam> beams = new ArrayList<Beam>(0);
    
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        group = new Group();
        pWidth = Screen.getPrimary().getBounds().getWidth();
        pHeight = Screen.getPrimary().getBounds().getHeight();
        ratio = pHeight/pWidth;
        uWidth = 20;
        uHeight = uWidth*ratio;

        Canvas canvas = new Canvas(pWidth, pHeight);
        gc = canvas.getGraphicsContext2D();
        draw();
        group.getChildren().add(canvas);
        
        makeSidebar();

        Button showOptions = new Button();
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), vbox);
        TranslateTransition buttonTransition = new TranslateTransition(Duration.millis(300),showOptions);
        transition.setToX(0);
        buttonTransition.setToX(0.2*pWidth);
        
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("Close.png")));
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        showOptions.setGraphic(icon);
        showOptions.setLayoutX(0.2*pWidth);
        showOptions.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (toggled){
                    ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("Open.png")));
                    icon.setFitWidth(24);
                    icon.setFitHeight(24);
                    ((Button)e.getSource()).setGraphic(icon);
                    ((Button)e.getSource()).setLayoutX(0);

                    transition.setToX(0);
                    buttonTransition.setToX(0.2*pWidth);
                    transition.setFromX(-vbox.getWidth());
                    buttonTransition.setFromX(0);

                    Duration time = transition.getCurrentTime();
                    transition.setRate(-1);
                    transition.playFrom(time);
                    buttonTransition.setRate(-1);
                    buttonTransition.playFrom(time);

                    toggled = false;
                    draw();
                }
                else{
                    ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("Close.png")));
                    icon.setFitWidth(24);
                    icon.setFitHeight(24);
                    ((Button)e.getSource()).setGraphic(icon);
                    ((Button)e.getSource()).setLayoutX(0.2*pWidth);

                    showOptions.setLayoutX(0);

                    transition.setToX(0);
                    buttonTransition.setToX(0.2*pWidth);
                    transition.setFromX(-vbox.getWidth());
                    buttonTransition.setFromX(0);       
            
                    Duration time = transition.getCurrentTime();
                    transition.setRate(1);
                    transition.playFrom(time);
                    time = buttonTransition.getCurrentTime();
                    buttonTransition.setRate(1);
                    buttonTransition.playFrom(time);
                    // canvasTransition.setRate(1);
                    // canvasTransition.playFrom(time);

                    toggled = true;
                    draw();
                }
            }
        });
        
        group.getChildren().add(showOptions);
        
        addSideBarEventHandlers();

        //Setting up the stage
        {
            scene = new Scene(group, pWidth, pHeight);
            scene.addEventHandler(MouseEvent.MOUSE_CLICKED, InputHandling.screenClick);
            scene.addEventHandler(MouseDragEvent.MOUSE_DRAGGED, InputHandling.screenDragged);
            scene.addEventHandler(MouseEvent.MOUSE_MOVED, InputHandling.mouseMoved);
            scene.addEventHandler(ScrollEvent.SCROLL, InputHandling.mouseScrolled);
            stage.setScene(scene);
            stage.setTitle("Truss");
            stage.setMaximized(true);
            stage.setResizable(false);
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);        
            stage.show();
        }
    }

    /**
     * Draws to screen
     */
    static void draw (){
        gc.clearRect(0, 0, pWidth, pHeight);
        uStep = findStep(uWidth/20);
        pStep = uStep*(pWidth/uWidth);
        
        if (visibleGrid){
            drawGrid();
        }
        else{
        }
    }

    /**
     * Draws the grid
     */
    static void drawGrid(){
        double k;
        int j = 0;
        if (0.5*pHeight + pOffsetY < 0.02*pHeight){
            k = 0;
            for (double i = (0.5*pWidth+pOffsetX); i < pWidth; i+=0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(i, 0, i, pHeight);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), i, (0.02*pHeight));
                    k += uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(i, 0, i, pHeight);
                }
                j++;
            }
            k = 0;
            j = 0;
            for (double i = (0.5*pWidth+pOffsetX); i > 0; i-=0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(i, 0, i, pHeight);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), i, (0.02*pHeight));
                    k -= uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(i, 0, i, pHeight);
                }
                j++;
            }
        }
        else if (0.5*pHeight + pOffsetY > 0.97*pHeight){
            k = 0;
            for (double i = (0.5*pWidth+pOffsetX); i < pWidth; i+=0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(i, 0, i, pHeight);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), i, (0.97*pHeight));
                    k += uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(i, 0, i, pHeight);
                }
                j++;
            }
            k = 0;
            j = 0;
            for (double i = (0.5*pWidth+pOffsetX); i > 0; i-=0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(i, 0, i, pHeight);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), i, (0.97*pHeight));
                    k -= uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(i, 0, i, pHeight);
                }
                j++;
            }
        }
        else{
            k = 0;
            for (double i = 0.5*pWidth+pOffsetX; i < pWidth; i += 0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(i, 0, i, pHeight);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), i, pOffsetY+0.5*pHeight);
                    k += uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(i, 0, i, pHeight);
                }
                j++;
            }
            k = 0;
            j = 0;
            for (double i = 0.5*pWidth+pOffsetX; i > 0; i -= 0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(i, 0, i, pHeight);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), i, pOffsetY+0.5*pHeight);
                    k -= uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(i, 0, i, pHeight);
                }
                j++;
            }
        }
        
        j = 0;
        if (0.5*pWidth + pOffsetX < 0.01*pWidth){
            k = 0;
            for (double i = 0.5*pHeight+pOffsetY; i < pHeight; i += 0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(0, i, pWidth, i);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), (0.01*pWidth), i);
                    k -= uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(0, i, pWidth, i);
                }
                j++;
            }
            k = 0;
            j = 0;
            for (double i = (0.5*pHeight+pOffsetY); i > 0; i-=0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(0, i, pWidth, i);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), (0.01*pWidth), i);
                    k += uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(0, i, pWidth, i);
                }
                j++;
            }
        }
        else if (0.5*pWidth + pOffsetX > 0.98*pWidth){
            k = 0;
            for (double i = (0.5*pHeight+pOffsetY); i < pHeight; i+=0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(0, i, pWidth, i);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), (0.98*pWidth), i);
                    k -= uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(0, i, pWidth, i);
                }
                j++;
            }
            j = 0;
            k = 0;
            for (double i = (0.5*pHeight+pOffsetY); i > 0; i-=0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(0, i, pWidth, i);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), (0.98*pWidth), i);
                    k += uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(0, i, pWidth, i);
                }
                j++;
            }
        }
        else{
            k = 0;
            for (double i = (0.5*pHeight+pOffsetY); i < pHeight; i+=0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(0, i, pWidth, i);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), (pOffsetX+0.5*pWidth), i);
                    k -= uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(0, i, pWidth, i);
                }
                j++;
            }
            j = 0;
            k = 0;
            for (double i = (0.5*pHeight+pOffsetY); i > 0; i-=0.2*pStep){
                if (j%5 == 0){
                    gc.setStroke(Color.LIGHTGREY);
                    gc.strokeLine(0, i, pWidth, i);
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeText(df.format(k), (pOffsetX+0.5*pWidth), i);
                    k += uStep;
                }
                else if (visibleMinorGrid){
                    gc.setStroke(Color.grayRgb(240));
                    gc.strokeLine(0, i, pWidth, i);
                }
                j++;
            }
        }
        
        gc.setStroke(Color.BLACK);
        if (pOffsetX > -0.5*pWidth && pOffsetX < 0.5*pWidth){
            gc.strokeLine(pOffsetX+pWidth/2, 0, pOffsetX+pWidth/2, pHeight);
        }
        if (pOffsetY > -0.5*pHeight && pOffsetY < 0.5*pHeight){
            gc.strokeLine(0, pOffsetY+pHeight/2, pWidth, pOffsetY+pHeight/2);
        }
    }

    /**
     * Finds an step value that starts with either 1, 2 or 5 and is one 
     * significant figure only. This makes the step sizes on the grid simpler to
     * follow.
     * 
     * @param val Value to round
     * @return double the rounded to sig. fig. value
     */
    static double findStep(double val){
        int exponent = getExp(val);
        val = val/(Math.pow(10, exponent));

        if (val < 1.5){
            val = 1;
        }
        else if (val < 3.5){
            val = 2;
        }
        else if (val < 7.5){
            val = 5;
        }
        else{
            val = 10;
        }
        return val*(Math.pow(10, exponent));
    }

    /**
     * Gets the exponent on 10 needed to put a double
     * into scientific notation.
     * 
     * @param val double to get exponent for
     * @return int The exponent on 10 for a double in scientific notation
     */
    static int getExp(double val){
        if (val < 0){
            val = -val;
        }
        if (val < 10 && val >= 1){
            return 0;
        }
        else if (val < 1){
            int i = 0;
            while (val < 1){
                val = val*10;
                ++i;
            }
            return -i;
        }
        else{
            int i = 0;
            while (val > 10){
                val = val/10;
                ++i;
            }
            return i;
        }
    }

    void makeSidebar(){ 
        
        vbox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        vbox.setPrefSize(0.2*pWidth, pHeight);
        
        DropShadow ds = new DropShadow();
        ds.setRadius(5);
        vbox.setEffect(ds);
        
        sidebarMakeNode();

        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        vbox.getChildren().add(separator2);

        sidebarMakeSupport();

        Separator separator = new Separator(Orientation.HORIZONTAL);
        vbox.getChildren().add(separator);

        sidebarMakeBeam();

        Separator separator3 = new Separator(Orientation.HORIZONTAL);
        vbox.getChildren().add(separator3);

        sidebarMakeForce();

        Separator separator4 = new Separator(Orientation.HORIZONTAL);
        vbox.getChildren().add(separator4);

        Button options = new Button("More Options");
        Button calc = new Button("Calculate!");

        buttons[OPTIONS] = options;
        buttons[CALC] = calc;
        
        BorderPane endButtons = new BorderPane(null, null, calc, null, options);

        vbox.getChildren().add(endButtons);

        Insets insets = new Insets(10, 10, 0, 10);
        for (Node n: vbox.getChildren()){
            VBox.setMargin(n, insets);
        }            
        
        group.getChildren().add(vbox);
    }

    void sidebarMakeNode(){
        Text newNode = new Text();
        newNode.setText("New Node");
        newNode.setFont(new Font("DejaVu Sans Mono", 20));
        newNode.setStroke(Color.grayRgb(60));
        newNode.setFill(Color.grayRgb(60));
        vbox.getChildren().add(newNode);

        Text nodeName = new Text();
        nodeName.setText("Enter Node Name: ");
        nodeName.setFont(new Font("DejaVu Sans Mono", 16));
        nodeName.setStroke(Color.grayRgb(60));
        nodeName.setFill(Color.grayRgb(60));
        vbox.getChildren().add(nodeName);

        TextField nodeNameField = new TextField();
        vbox.getChildren().add(nodeNameField);
        textfields[NODENAME] = nodeNameField;
        
        Text coordinate = new Text();
        coordinate.setText("Enter Coordinates: ");
        coordinate.setFont(new Font("DejaVu Sans Mono", 16));
        coordinate.setStroke(Color.grayRgb(60));
        coordinate.setFill(Color.grayRgb(60));
        vbox.getChildren().add(coordinate);

        HBox coordinates = new HBox();
        Text xy = new Text();
        xy.setText("(x, y) = ( ");
        xy.setFont(new Font("DejaVu Sans Mono", 14));
        xy.setStroke(Color.grayRgb(60));
        xy.setFill(Color.grayRgb(60));
        coordinates.getChildren().add(xy);

        TextField xPos = new TextField();
        xPos.setMaxWidth(0.05*pWidth);
        coordinates.getChildren().add(xPos);
        textfields[NODEX] = xPos;
        
        Text m = new Text();
        m.setText(" m, ");
        m.setFont(new Font("DejaVu Sans Mono", 14));
        m.setStroke(Color.grayRgb(60));
        m.setFill(Color.grayRgb(60));
        coordinates.getChildren().add(m);

        TextField yPos = new TextField();
        yPos.setMaxWidth(0.05*pWidth);
        coordinates.getChildren().add(yPos);
        textfields[NODEY] = yPos;

        Text end = new Text();
        end.setText(" m)");
        end.setFont(new Font("DejaVu Sans Mono", 14));
        end.setStroke(Color.grayRgb(60));
        end.setFill(Color.grayRgb(60));
        coordinates.getChildren().add(end);

        vbox.getChildren().add(coordinates);

        Button addNode = new Button("Add Node");
        buttons[ADDNODE] = addNode;
        BorderPane addNodeButton = new BorderPane(addNode);
        vbox.getChildren().add(addNodeButton);
    }

    void sidebarMakeSupport(){
        Text newSupport = new Text();
        newSupport.setText("New Support");
        newSupport.setFont(new Font("DejaVu Sans Mono", 20));
        newSupport.setStroke(Color.grayRgb(60));
        newSupport.setFill(Color.grayRgb(60));
        vbox.getChildren().add(newSupport);
        
        Text nodeName = new Text();
        nodeName.setText("Enter Support Name: ");
        nodeName.setFont(new Font("DejaVu Sans Mono", 16));
        nodeName.setStroke(Color.grayRgb(60));
        nodeName.setFill(Color.grayRgb(60));
        vbox.getChildren().add(nodeName);

        TextField nodeNameField = new TextField();
        vbox.getChildren().add(nodeNameField);
        textfields[SUPPORTNAME] = nodeNameField;
        
        HBox coordinates = new HBox();
        Text xy = new Text();
        xy.setText("(x, y) = ( ");
        xy.setFont(new Font("DejaVu Sans Mono", 14));
        xy.setStroke(Color.grayRgb(60));
        xy.setFill(Color.grayRgb(60));
        coordinates.getChildren().add(xy);

        TextField xPos = new TextField();
        xPos.setMaxWidth(0.05*pWidth);
        coordinates.getChildren().add(xPos);
        textfields[SUPPORTX] = xPos;
        
        Text m = new Text();
        m.setText(" m, ");
        m.setFont(new Font("DejaVu Sans Mono", 14));
        m.setStroke(Color.grayRgb(60));
        m.setFill(Color.grayRgb(60));
        coordinates.getChildren().add(m);

        TextField yPos = new TextField();
        yPos.setMaxWidth(0.05*pWidth);
        coordinates.getChildren().add(yPos);
        textfields[SUPPORTY] = yPos;

        Text end = new Text();
        end.setText(" m)");
        end.setFont(new Font("DejaVu Sans Mono", 14));
        end.setStroke(Color.grayRgb(60));
        end.setFill(Color.grayRgb(60));
        coordinates.getChildren().add(end);

        vbox.getChildren().add(coordinates);

        HBox selectSupport = new HBox();

        Text selectType = new Text();
        selectType.setText("Type of Support: ");
        selectType.setFont(new Font("DejaVu Sans Mono", 16));
        selectType.setStroke(Color.grayRgb(60));
        selectType.setFill(Color.grayRgb(60));
        vbox.getChildren().add(selectSupport);

        selectSupport.getChildren().add(selectType);

        ImageView rollerSupp = new ImageView(new Image(getClass().getResourceAsStream("RollerSupport.png")));
        rollerSupp.setFitWidth(25);
        rollerSupp.setFitHeight(25);
        ImageView pinSupp = new ImageView(new Image(getClass().getResourceAsStream("PinnedSupport.png")));
        pinSupp.setFitWidth(25);
        pinSupp.setFitHeight(25);
        ImageView fixedSupp = new ImageView(new Image(getClass().getResourceAsStream("FixedSupport.png")));
        fixedSupp.setFitWidth(25);
        fixedSupp.setFitHeight(25);

        ToggleGroup supports = new ToggleGroup();
        RadioButton rollerSupport = new RadioButton();
        rollerSupport.setGraphic(rollerSupp);
        rollerSupport.setToggleGroup(supports);
        RadioButton pinnedSupport = new RadioButton();
        pinnedSupport.setGraphic(pinSupp);
        pinnedSupport.setToggleGroup(supports);
        RadioButton fixedSupport = new RadioButton();
        fixedSupport.setGraphic(fixedSupp);
        fixedSupport.setToggleGroup(supports);

        selectSupport.getChildren().add(rollerSupport);
        selectSupport.getChildren().add(pinnedSupport);
        selectSupport.getChildren().add(fixedSupport);

        Insets insets = new Insets(0, 0, 0, 10);
        HBox.setMargin(rollerSupport, insets);
        HBox.setMargin(pinnedSupport, insets);
        HBox.setMargin(fixedSupport, insets);

        rdButtons[ROLLER] = rollerSupport;
        rdButtons[PIN] = pinnedSupport;
        rdButtons[FIXED] = fixedSupport;
                
        Button addNode = new Button("Add Support");
        BorderPane addNodeButton = new BorderPane(addNode);
        buttons[ADDSUPP] = addNode;
        vbox.getChildren().add(addNodeButton);
    }

    void sidebarMakeBeam(){
        Text addBeam = new Text();
        addBeam.setText("New Beam");
        addBeam.setFont(new Font("DejaVu Sans Mono", 20));
        addBeam.setStroke(Color.grayRgb(60));
        addBeam.setFill(Color.grayRgb(60));
        vbox.getChildren().add(addBeam);

        HBox firstNode = new HBox();

        Text nodeOne = new Text();
        nodeOne.setText("First Node: ");
        nodeOne.setFont(new Font("DejaVu Sans Mono", 14));
        nodeOne.setStroke(Color.grayRgb(60));
        nodeOne.setFill(Color.grayRgb(60));
        firstNode.getChildren().add(nodeOne);
        
        TextField nodeOneField = new TextField();
        textfields[BEAMNODEA] = nodeOneField;
        firstNode.getChildren().add(nodeOneField);

        vbox.getChildren().add(firstNode);

        HBox SecondNode = new HBox();

        Text nodeTwo = new Text();
        nodeTwo.setText("Second Node: ");
        nodeTwo.setFont(new Font("DejaVu Sans Mono", 14));
        nodeTwo.setStroke(Color.grayRgb(60));
        nodeTwo.setFill(Color.grayRgb(60));
        SecondNode.getChildren().add(nodeTwo);
        
        TextField nodeTwoField = new TextField();
        SecondNode.getChildren().add(nodeTwoField);
        textfields[BEAMNODEB] = nodeTwoField;

        vbox.getChildren().add(SecondNode);
        
        Button addBeamButton = new Button("Add Beam");
        BorderPane beamButton = new BorderPane(addBeamButton);
        buttons[ADDBEAM] = addBeamButton;
        vbox.getChildren().add(beamButton);
    }

    void sidebarMakeForce(){
        Text addForce = new Text();
        addForce.setText("New Force");
        addForce.setFont(new Font("DejaVu Sans Mono", 20));
        addForce.setStroke(Color.grayRgb(60));
        addForce.setFill(Color.grayRgb(60));
        vbox.getChildren().add(addForce);

        HBox nodeNameH = new HBox();

        Text nodeName = new Text();
        nodeName.setText("Specify Node: ");
        nodeName.setFont(new Font("DejaVu Sans Mono", 14));
        nodeName.setStroke(Color.grayRgb(60));
        nodeName.setFill(Color.grayRgb(60));
        nodeNameH.getChildren().add(nodeName);
        
        TextField nodeNameField = new TextField();
        textfields[FORCENODE] = nodeNameField;
        nodeNameH.getChildren().add(nodeNameField);

        vbox.getChildren().add(nodeNameH);

        HBox forceAmountH = new HBox();
        
        Text forceAm = new Text();
        forceAm.setText("Force Magnitude: ");
        forceAm.setFont(new Font("DejaVu Sans Mono", 14));
        forceAm.setStroke(Color.grayRgb(60));
        forceAm.setFill(Color.grayRgb(60));
        forceAmountH.getChildren().add(forceAm);

        TextField forceAmField = new TextField();
        textfields[FORCEVAL] = forceAmField;
        forceAmountH.getChildren().add(forceAmField);

        Text end = new Text();
        end.setText(" kN");
        end.setFont(new Font("DejaVu Sans Mono", 14));
        end.setStroke(Color.grayRgb(60));
        end.setFill(Color.grayRgb(60));
        forceAmountH.getChildren().add(end);

        vbox.getChildren().add(forceAmountH);

        HBox forceDirH = new HBox();

        Text forceDir = new Text();
        forceDir.setText("Force Direction: ");
        forceDir.setFont(new Font("DejaVu Sans Mono", 14));
        forceDir.setStroke(Color.grayRgb(60));
        forceDir.setFill(Color.grayRgb(60));
        forceDirH.getChildren().add(forceDir);
        
        TextField forceDirField = new TextField();
        textfields[FORCEDIR] = forceDirField;
        forceDirH.getChildren().add(forceDirField);

        vbox.getChildren().add(forceDirH);
        
        Button addForceButton = new Button("Add Force");
        BorderPane forceButton = new BorderPane(addForceButton);
        buttons[ADDFORCE] = addForceButton;
        vbox.getChildren().add(forceButton);
    }

    void addSideBarEventHandlers(){  
        buttons[ADDNODE].setOnAction(InputHandling.addNodeEventHandler);
        buttons[ADDBEAM].setOnAction(InputHandling.addBeamEventHandler);
        buttons[ADDFORCE].setOnAction(InputHandling.addForceEventHandler);
        buttons[ADDSUPP].setOnAction(InputHandling.addSupportEventHandler);

        textfields[NODENAME].setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent ae){
                textfields[NODEX].requestFocus();
            }
        });
        textfields[NODEX].setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent ae){
                textfields[NODEY].requestFocus();
            }
        });
        textfields[NODEY].setOnAction(InputHandling.addNodeEventHandler);
        textfields[SUPPORTNAME].setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent ae){
                textfields[SUPPORTX].requestFocus();
            }
        });
        textfields[SUPPORTX].setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent ae){
                textfields[SUPPORTY].requestFocus();
            }
        });
        textfields[SUPPORTY].setOnAction(InputHandling.addSupportEventHandler);
        textfields[BEAMNODEA].setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent ae){
                textfields[BEAMNODEB].requestFocus();
            }
        });
        textfields[BEAMNODEB].setOnAction(InputHandling.addBeamEventHandler);
        textfields[FORCENODE].setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent ae){
                textfields[FORCEVAL].requestFocus();
            }
        });
        textfields[FORCEDIR].setOnAction(InputHandling.addForceEventHandler);
    }
}