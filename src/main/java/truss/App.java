package truss;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.event.ActionEvent;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    static VBox helpVBox = new VBox();

    static DecimalFormat df = new DecimalFormat("#");
    
    static final int NODENAME = 0, NODEX = 1, NODEY = 2, BEAMNODEA = 3, BEAMNODEB = 4, FORCENODE = 5, FORCEVAL = 6, FORCEDIR = 7;
    static TextField[] textfields = new TextField[8];

    static final int ADDNODE = 0, ADDBEAM = 1, ADDFORCE = 2, OPTIONS = 3, CALC = 4, RESET = 5;
    static Button[] buttons = new Button[6];

    static final int ROLLER = 0, PIN = 1, FIXED = 2, NOTSUPP = 3;
    static RadioButton[] rdButtons = new RadioButton[3];

    static boolean visibleGrid = true;
    static boolean visibleMinorGrid = true;
    static boolean toggled = true;

    static ArrayList<Joint> nodes = new ArrayList<Joint>(0);
    static Map<String, Joint> nodeMap = new HashMap<String, Joint>(0);
    static ArrayList<Beam> beams = new ArrayList<Beam>(0);
    static ArrayList<Arrow> reactionForces = new ArrayList<Arrow>(0);
    static int indexOfPin = -1;
    static int indexOfRoller = -1;
    
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
        try{
            makeHelp();
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());
        };
        makeSidebar();

        Button showOptions = new Button();
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), vbox);
        TranslateTransition buttonTransition = new TranslateTransition(Duration.millis(300),showOptions);
        transition.setToX(0);
        buttonTransition.setToX(0.2*pWidth);
        
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/Close.png")));
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        showOptions.setGraphic(icon);
        showOptions.setLayoutX(0.2*pWidth);
        showOptions.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (toggled){
                    ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/Open.png")));
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
                    ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/Close.png")));
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
            scene.getStylesheets().add("/textformats.css");
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

        Separator separator = new Separator(Orientation.HORIZONTAL);
        vbox.getChildren().add(separator);

        sidebarMakeBeam();

        Separator separator3 = new Separator(Orientation.HORIZONTAL);
        vbox.getChildren().add(separator3);

        sidebarMakeForce();

        Separator separator4 = new Separator(Orientation.HORIZONTAL);
        vbox.getChildren().add(separator4);

        Button options = new Button("Help");
        Button calc = new Button("Calculate!");
        Button reset = new Button("Reset");
        options.setId("helpButton");
        calc.setId("calculateButton");
        reset.setId("resetButton");
        
        buttons[OPTIONS] = options;
        buttons[CALC] = calc;
        buttons[RESET] = reset;
        
        BorderPane endButtons = new BorderPane(reset, null, calc, null, options);

        vbox.getChildren().add(endButtons);

        Insets insets = new Insets(10, 10, 0, 10);
        for (Node n: vbox.getChildren()){
            VBox.setMargin(n, insets);
        }            
        
        group.getChildren().add(vbox);
    }

    void sidebarMakeNode(){
        Text newNode = new Text("New Joint");
        newNode.setId("largeTextSideBar");
        vbox.getChildren().add(newNode);

        Text nodeName = new Text("Enter Joint Name: ");
        nodeName.setId("smallTextSideBar");
        vbox.getChildren().add(nodeName);

        TextField nodeNameField = new TextField();
        vbox.getChildren().add(nodeNameField);
        textfields[NODENAME] = nodeNameField;
        
        Text coordinate = new Text("Enter Coordinates: ");
        coordinate.setId("smallTextSideBar");
        vbox.getChildren().add(coordinate);

        HBox coordinates = new HBox();
        Text xy = new Text("(x, y) = ( ");
        xy.setId("smallerTextSideBar");
        coordinates.getChildren().add(xy);

        TextField xPos = new TextField();
        xPos.setMaxWidth(0.05*pWidth);
        coordinates.getChildren().add(xPos);
        textfields[NODEX] = xPos;
        
        Text m = new Text(" m, ");
        m.setId("smallerTextSideBar");
        coordinates.getChildren().add(m);

        TextField yPos = new TextField();
        yPos.setMaxWidth(0.05*pWidth);
        coordinates.getChildren().add(yPos);
        textfields[NODEY] = yPos;

        Text end = new Text(" m)");
        end.setId("smallerTextSideBar");
        coordinates.getChildren().add(end);

        vbox.getChildren().add(coordinates);

        HBox selectSupport = new HBox();

        Text selectType = new Text("Type of Support: ");
        selectType.setId("smallTextSideBar");
        vbox.getChildren().add(selectSupport);

        selectSupport.getChildren().add(selectType);

        ImageView rollerSupp = new ImageView(new Image(getClass().getResourceAsStream("/RollerSupport.png")));
        rollerSupp.setFitWidth(25);
        rollerSupp.setFitHeight(25);
        ImageView pinSupp = new ImageView(new Image(getClass().getResourceAsStream("/PinnedSupport.png")));
        pinSupp.setFitWidth(25);
        pinSupp.setFitHeight(25);
        ImageView fixedSupp = new ImageView(new Image(getClass().getResourceAsStream("/FixedSupport.png")));
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
        //Maybe will add fixed support stuff later
        // selectSupport.getChildren().add(fixedSupport);

        Insets insets = new Insets(0, 0, 0, 10);
        HBox.setMargin(rollerSupport, insets);
        HBox.setMargin(pinnedSupport, insets);
        //HBox.setMargin(fixedSupport, insets);

        rdButtons[ROLLER] = rollerSupport;
        rdButtons[PIN] = pinnedSupport;
        rdButtons[FIXED] = fixedSupport;

        Button addNode = new Button("Add Joint");
        buttons[ADDNODE] = addNode;
        BorderPane addNodeButton = new BorderPane(addNode);
        vbox.getChildren().add(addNodeButton);
    }

    void sidebarMakeBeam(){  
        Text addBeam = new Text("New Beam");
        addBeam.setId("largeTextSideBar");
        vbox.getChildren().add(addBeam);

        HBox firstNode = new HBox();

        Text nodeOne = new Text("First Joint: ");
        nodeOne.setId("smallerTextSideBar");
        firstNode.getChildren().add(nodeOne);
        
        TextField nodeOneField = new TextField();
        textfields[BEAMNODEA] = nodeOneField;
        firstNode.getChildren().add(nodeOneField);

        vbox.getChildren().add(firstNode);

        HBox SecondNode = new HBox();

        Text nodeTwo = new Text("Second Joint: ");
        nodeTwo.setId("smallerTextSideBar");
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
        Text addForce = new Text("New Force");
        addForce.setId("largeTextSideBar");
        vbox.getChildren().add(addForce);

        HBox nodeNameH = new HBox();

        Text nodeName = new Text("Specify Joint: ");
        nodeName.setId("smallerTextSideBar");
        nodeNameH.getChildren().add(nodeName);
        
        TextField nodeNameField = new TextField();
        textfields[FORCENODE] = nodeNameField;
        nodeNameH.getChildren().add(nodeNameField);

        vbox.getChildren().add(nodeNameH);

        HBox forceAmountH = new HBox();
        
        Text forceAm = new Text("Force Magnitude: ");
        forceAm.setId("smallerTextSideBar");
        forceAmountH.getChildren().add(forceAm);

        TextField forceAmField = new TextField();
        textfields[FORCEVAL] = forceAmField;
        forceAmountH.getChildren().add(forceAmField);

        Text end = new Text(" kN");
        end.setId("smallerTextSideBar");
        forceAmountH.getChildren().add(end);

        vbox.getChildren().add(forceAmountH);

        HBox forceDirH = new HBox();

        Text forceDir = new Text("Force Direction: ");
        forceDir.setId("smallerTextSideBar");
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
        buttons[CALC].setOnAction(InputHandling.calculate);
        buttons[OPTIONS].setOnAction(InputHandling.helpAlert);
        buttons[RESET].setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent ae){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "This will reset and remove ALL joints, beams and forces. Are you sure you want to continue?", ButtonType.OK, ButtonType.CANCEL);
                alert.setHeaderText(null);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    InputHandling.reset();
                }
            }
        });
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
    
    /**
     * Verifies if the system of joints, supports and beams could be statically solvable
     */
    static boolean verify(){
        int cfRoller = 0, cfPin = 0, cfFixed = 0;
        for (Joint j: nodes){
            switch(j.getType()){
                case ROLLER:
                    cfRoller++;
                    indexOfRoller = nodes.indexOf(j);
                    break;
                case PIN:
                    indexOfPin = nodes.indexOf(j);
                    cfPin++;
                    break;
                case FIXED:
                    cfFixed++;
            }

            if (j.getBeams().size()<2){
                InputHandling.showError("Each node/support must have at least 2 attached beams.");
                return false;
            }
        }

        if (!(cfRoller >= 1 && cfPin == 1 && cfFixed == 0) && !(cfRoller == 0 && cfPin == 0 && cfFixed == 1)){
            InputHandling.showError("There must be either JUST one roller and one pin support OR JUST one fixed support.");
            return false;
        }
        
        if (beams.size()+3 < nodes.size()*2){
            InputHandling.showError("Not enough nodes to statically solve the truss.");
            return false;
        }
        
        return true;
    }

    static void calculate(){
        double matrix[][] = new double[nodes.size()*2][beams.size()+3+1];
        double fx = 0;
        double fy = 0;
        Joint pin = nodes.get(indexOfPin);
        Joint rol = nodes.get(indexOfRoller);

        int matWid = matrix[0].length;
        Joint curJoint;
        double temp;

        final int PIN_PERPENDICULAR = matWid-4;
        final int PIN_PARALLEL = matWid-3;
        final int ROLLER_PARALLEL = matWid-2;
        final int EXTERNAL_FORCE = matWid-1;
        
        //All values are rounded to the nearest 10^-14 since slight errors in extremely
        //small values (order of 10^-16) cause massive problem, so by rounding those 
        //values are turned to 0 (which is what they should be if not for unavoidable
        //floating point precision errors)
        for (int i = 0; i < matrix.length; i+=2){
            curJoint = nodes.get(i/2);
            if (curJoint == pin){
                temp =  Math.sin(Math.toRadians(curJoint.getAngle()));
                matrix[i][PIN_PARALLEL] = 1e-14*Math.round(1e14*temp);
                matrix[i+1][PIN_PERPENDICULAR] = 1e-14*Math.round(1e14*temp);

                temp = Math.cos(Math.toRadians(curJoint.getAngle()));
                matrix[i+1][PIN_PARALLEL] = 1e-14*Math.round(1e14*temp);
                matrix[i][PIN_PERPENDICULAR] = 1e-14*Math.round(1e14*temp);
            }
            else if (curJoint == rol){
                temp = Math.sin(Math.toRadians(curJoint.getAngle()));
                matrix[i][ROLLER_PARALLEL] = 1e-14*Math.round(1e14*temp);

                temp = Math.cos(Math.toRadians(curJoint.getAngle()));
                matrix[i+1][ROLLER_PARALLEL] = 1e-14*Math.round(1e14*temp);
            }
            fx = 0;
            fy = 0;
            for (int j = 0; j < curJoint.getForceVals().size(); j++){
                double F = curJoint.getForceVals().get(j);
                temp = Math.cos(Math.toRadians(curJoint.getForceDirs().get(j)));
                fx -= F*temp;

                temp = Math.sin(Math.toRadians(curJoint.getForceDirs().get(j)));             
                fy -= F*temp;
            }
            matrix[i][EXTERNAL_FORCE] = fy;
            matrix[i+1][EXTERNAL_FORCE] = fx;
            
            for (Beam b: curJoint.getBeams()){
                Joint a = b.getJointA() == curJoint ? b.getJointB() : b.getJointA();
                double theta = Math.atan2(a.getUY()-curJoint.getUY(), a.getUX()-curJoint.getUX());
                temp = Math.sin(theta);
                matrix[i][beams.indexOf(b)] = 1e-14*Math.round(1e14*temp);

                temp = Math.cos(theta);
                matrix[i+1][beams.indexOf(b)] = 1e-14*Math.round(1e14*temp);
            }
        }

        matrix = gaussianElimination(matrix);
        DecimalFormat dff = new DecimalFormat("#.####");
        for (int i = 0; i < beams.size(); i++){
            beams.get(i).setForce(dff.format(matrix[i][matrix[i].length-1])+" kN");
        }

        for (Arrow a: reactionForces){
            group.getChildren().remove(a);
        }

        reactionForces.clear();
        
        //forces with a magnitude less than 5e-6 kN, or 5 mN are not shown
        //as those forces are less than the 1/10th of the force exerted on
        //a US quarter by gravity, so these forces are considered entirely
        //insignificant
        double force = matrix[matrix.length-2][matrix[0].length-1];
        if (force < 5e-6 && force > -5e-6){}
        else if (force > 0){
            reactionForces.add(new Arrow(pin, -pin.getAngle(), force, true));   
        }
        else if (force < 0){
            reactionForces.add(new Arrow(pin, pin.getAngle(), -force, true));             
        }
        
        force = matrix[matrix.length-3][matrix[0].length-1];
        if (force < 5e-6 && force > -5e-6){}
        else if (force > 0){
            reactionForces.add(new Arrow(pin, pin.getAngle()-90, force, true));
        }
        else if (force < 0){
            reactionForces.add(new Arrow(pin, -pin.getAngle()+90, -force, true));
        }

        force = matrix[matrix.length-1][matrix[0].length-1];
        if (force < 5e-6 && force > -5e-6){}
        else if (force > 0){
            reactionForces.add(new Arrow(rol, -rol.getAngle(), force, true));
        }
        else if (force < 0){
            reactionForces.add(new Arrow(rol, rol.getAngle(), -force, true));
        }

        for (Arrow a: reactionForces){
            group.getChildren().add(a);
        }
    }

    /**
     * Makes the help menu
     */
    static void makeHelp() throws IOException{
        Scanner in = new Scanner(App.class.getResourceAsStream("/helpAssets/HelpMenu.txt"), "UTF-8");
        Text text;

        //Title
        {
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.MEDIUM, 24));
            helpVBox.getChildren().add(text);

            Separator separator = new Separator(Orientation.HORIZONTAL);
            helpVBox.getChildren().add(separator);
        }

        //Intro
        {
            for (int i = 0; i < 18; ++i){
                text = new Text(in.nextLine());
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 14));
                helpVBox.getChildren().add(text);
            }
        }

        //Joint
        {
            //Joint intro
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 20));
            helpVBox.getChildren().add(text);
            Separator separator = new Separator(Orientation.HORIZONTAL);
            helpVBox.getChildren().add(separator);
            
            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);
                        
            Image img = new Image(App.class.getResourceAsStream(in.nextLine()));
            ImageView imgView = new ImageView(img);
            double ratio = img.getHeight()/img.getWidth();
            imgView.setFitWidth(0.25*pWidth);
            imgView.setFitHeight(0.25*pWidth*ratio);
            helpVBox.getChildren().add(imgView);

            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);

            for (int i = 0; i < 3; i++){
                text = new Text(in.nextLine());
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 16));
                helpVBox.getChildren().add(text);

                separator = new Separator(Orientation.HORIZONTAL);
                separator.setMaxWidth(text.getLayoutBounds().getWidth());
                helpVBox.getChildren().add(separator);
        
                text = new Text(in.nextLine());
                text.setWrappingWidth(pWidth/2);
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
                helpVBox.getChildren().add(text);
                
                img = new Image(App.class.getResourceAsStream(in.nextLine()));
                imgView = new ImageView(img);
                ratio = img.getHeight()/img.getWidth();
                if (i != 2){
                    imgView.setFitWidth(0.375*pWidth);
                    imgView.setFitHeight(0.375*pWidth*ratio);
                }
                else{
                    imgView.setFitWidth(0.125*pWidth);
                    imgView.setFitHeight(0.125*pWidth*ratio);
                }
                helpVBox.getChildren().add(imgView);

                text = new Text(in.nextLine());
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
                helpVBox.getChildren().add(text);
            }
        }

        //Beam
        {
            //Force intro
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 20));
            helpVBox.getChildren().add(text);
            Separator separator = new Separator(Orientation.HORIZONTAL);
            helpVBox.getChildren().add(separator);
            
            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);
                        
            Image img = new Image(App.class.getResourceAsStream(in.nextLine()));
            ImageView imgView = new ImageView(img);
            double ratio = img.getHeight()/img.getWidth();
            imgView.setFitWidth(0.25*pWidth);
            imgView.setFitHeight(0.25*pWidth*ratio);
            helpVBox.getChildren().add(imgView);

            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);

            for (int i = 0; i < 2; i++){
                text = new Text(in.nextLine());
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 16));
                helpVBox.getChildren().add(text);

                separator = new Separator(Orientation.HORIZONTAL);
                separator.setMaxWidth(text.getLayoutBounds().getWidth());
                helpVBox.getChildren().add(separator);
        
                text = new Text(in.nextLine());
                text.setWrappingWidth(pWidth/2);
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
                helpVBox.getChildren().add(text);
                
                img = new Image(App.class.getResourceAsStream(in.nextLine()));
                imgView = new ImageView(img);
                ratio = img.getHeight()/img.getWidth();
                imgView.setFitWidth(0.375*pWidth/(i+1));
                imgView.setFitHeight(0.375*pWidth*ratio/(i+1));
                helpVBox.getChildren().add(imgView);

                text = new Text(in.nextLine());
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
                helpVBox.getChildren().add(text);
            }
        }
        
        //Force
        {
            //Force intro
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 20));
            helpVBox.getChildren().add(text);
            Separator separator = new Separator(Orientation.HORIZONTAL);
            helpVBox.getChildren().add(separator);
            
            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);
                        
            Image img = new Image(App.class.getResourceAsStream(in.nextLine()));
            ImageView imgView = new ImageView(img);
            double ratio = img.getHeight()/img.getWidth();
            imgView.setFitWidth(0.125*pWidth);
            imgView.setFitHeight(0.125*pWidth*ratio);
            helpVBox.getChildren().add(imgView);

            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);

            for (int i = 0; i < 2; i++){
                text = new Text(in.nextLine());
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 16));
                helpVBox.getChildren().add(text);

                separator = new Separator(Orientation.HORIZONTAL);
                separator.setMaxWidth(text.getLayoutBounds().getWidth());
                helpVBox.getChildren().add(separator);
        
                text = new Text(in.nextLine());
                text.setWrappingWidth(pWidth/2);
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
                helpVBox.getChildren().add(text);

                text = new Text(in.nextLine());
                text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
                helpVBox.getChildren().add(text);
            }
        }

        //Solve Truss
        {
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 20));
            helpVBox.getChildren().add(text);
            Separator separator = new Separator(Orientation.HORIZONTAL);
            helpVBox.getChildren().add(separator);
            
            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);
            
            Image img = new Image(App.class.getResourceAsStream(in.nextLine()));
            ImageView imgView = new ImageView(img);
            double ratio = img.getHeight()/img.getWidth();
            imgView.setFitWidth(0.45*pWidth);
            imgView.setFitHeight(0.45*pWidth*ratio);
            helpVBox.getChildren().add(imgView);

            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);
        }

        //Other
        {
            //Other title
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 20));
            helpVBox.getChildren().add(text);
            Separator separator = new Separator(Orientation.HORIZONTAL);
            helpVBox.getChildren().add(separator);
            
            //Sidebar
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 16));
            helpVBox.getChildren().add(text);

            separator = new Separator(Orientation.HORIZONTAL);
            separator.setMaxWidth(text.getLayoutBounds().getWidth());
            helpVBox.getChildren().add(separator);
    
            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);
            
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);

            //Grid Display
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 16));
            helpVBox.getChildren().add(text);
            
            separator = new Separator(Orientation.HORIZONTAL);
            separator.setMaxWidth(text.getLayoutBounds().getWidth());
            helpVBox.getChildren().add(separator);
    
            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);
            
            Image shg = new Image(App.class.getResourceAsStream(in.nextLine()));
            ImageView showHideGrid = new ImageView(shg);
            double ratio = shg.getHeight()/shg.getWidth();
            showHideGrid.setFitWidth(0.25*pWidth);
            showHideGrid.setFitHeight(0.25*pWidth*ratio);
            helpVBox.getChildren().add(showHideGrid);

            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);
            
            //Reset
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 16));
            helpVBox.getChildren().add(text);
            
            separator = new Separator(Orientation.HORIZONTAL);
            separator.setMaxWidth(text.getLayoutBounds().getWidth());
            helpVBox.getChildren().add(separator);
    
            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);

            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);

            //Error Message
            text = new Text(in.nextLine());
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 16));
            helpVBox.getChildren().add(text);
            
            separator = new Separator(Orientation.HORIZONTAL);
            separator.setMaxWidth(text.getLayoutBounds().getWidth());
            helpVBox.getChildren().add(separator);
    
            text = new Text(in.nextLine());
            text.setWrappingWidth(pWidth/2);
            text.setFont(Font.font("DejaVu Sans Mono", FontWeight.NORMAL, 12));
            helpVBox.getChildren().add(text);
            
            Image errorImg = new Image(App.class.getResourceAsStream(in.nextLine()));
            ImageView errorImageView = new ImageView(errorImg);
            ratio = errorImg.getHeight()/errorImg.getWidth();
            errorImageView.setFitWidth(0.25*pWidth);
            errorImageView.setFitHeight(0.25*pWidth*ratio);
            helpVBox.getChildren().add(errorImageView);
        }

        in.close();
    }

    static double[][] gaussianElimination(double [][] matrix){
        int c = 0;
        boolean check = false;
        int rows = matrix.length;
        int cols = matrix[0].length;

        //forward step
        for (int i = 0; i < rows; i++){
            check = false;
            while(check == false){
                if (c >= cols){
                    break;
                }
                if (matrix[i][c] == 0){
                    for (int j = i+1; j < rows; j++){
                        if (matrix[j][c] != 0){
                            for (int k = 0; k < cols; k++){
                                double temp = matrix[i][k];
                                matrix[i][k] = matrix[j][k];
                                matrix[j][k] = temp;
                                check = true;
                            }
                            break;
                        }
                    }
                }
                else{
                    check = true;
                }
                if (check == false){
                    c++;
                }
            }
            if (c >= cols){
                break;
            }

            for (int j = i+1; j < rows; j++){
                if (i != j){
                    double div = -matrix[j][c]/matrix[i][c];
                    for (int k = 0; k < cols; k++){
                        matrix[j][k] += matrix[i][k]*div;
                    }
                }
            }
            c++;
        }

        //backward step
        c = 0;
        for (int i = 0; i < rows; i++){
            while (matrix[i][c] == 0){
                if (c >= cols){
                    break;
                }
                c++;
            }
            if (c >= cols){
                break;
            }

            double div = matrix[i][c];
            for (int k = 0; k < cols; k++){
                matrix[i][k] /= div;
            }

            for (int j = 0; j < rows; j++){
                if (i != j){
                    div = -matrix[j][c]/matrix[i][c];
                    for (int k = 0; k < cols; k++){
                        matrix[j][k] += div*matrix[i][k];
                    }
                }
            }
        }

        return matrix;
    }
}