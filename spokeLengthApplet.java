import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import javax.swing.*;
import javax.swing.border.*;
import java.text.DecimalFormat;
import java.math.RoundingMode;

// For image processing.
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.*;

// For dialog.
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


public class SpokeLengthApplet extends JApplet implements ActionListener {

  // Right panel card layout choices.
  final static String FRONTPANEL = "front";
  final static String REARPANEL = "rear";
  final static String CALCPANEL = "calc";
  final static String IMAGEPANEL = "image";

  // Animiation images.
  final static int NBR_IMAGES = 36;
  int imageNbr;
  Timer imageTimer;
  private BufferedImage images [] = new BufferedImage [NBR_IMAGES];

  // Help page images
  ImageIcon frontHelpImage;
  ImageIcon rearHelpImage;

  // Global controls.
  JPanel rightPanel;
  JButton frontButton;
  JButton rearButton;
  JButton calcButton;
  JLabel imageLabel;

  // Misc constants.
  String[] crossStrings = { "--", "2x", "3x", "4x" };
  String[] holesStrings = { "--", "28", "32", "36" };
  Color LIGHT_RED = new Color(255, 196, 196);
  Color TEXTFIELD_COLOR = UIManager.getColor ( "TextField.background" );
  Color COMBO_COLOR = UIManager.getColor ( "ComboBox.background" );

  // Global input fields.
  JTextField frontRimDiameter;
  double frontRimDiameterValue = 0;
  JTextField frontHubDiameter;
  double frontHubDiameterValue = 0;
  JTextField frontHubWidth;
  double frontHubWidthValue = 0;
  JComboBox<String> frontCross;
  int frontCrossValue = 0;
  JComboBox<String> frontHoles;
  int frontHolesValue = 0;

  JTextField rearRimDiameter;
  double rearRimDiameterValue = 0;
  JTextField rearHubDiameter;
  double rearHubDiameterValue = 0;
  JTextField rearHubWidth;
  double rearHubWidthValue = 0;
  JTextField rearLeftOffset;
  double rearLeftOffsetValue = 0;
  JTextField rearRightOffset;
  double rearRightOffsetValue = 0;
  JComboBox<String> rearCross;
  int rearCrossValue = 0;
  JComboBox<String> rearHoles;
  int rearHolesValue = 0;

  // Global output fields.
  JTextArea msgText;
  String defaultMsg;
  JTextField frontLength;
  JTextField leftRearLength;
  JTextField rightRearLength;

  // *******************************************************************

  public void init() {

    Container content = getContentPane();

    // MAIN panel so we can position other containers.
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout (new BorderLayout(0, 0));
    mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    content.add(mainPanel);

    // The BUTTON panel holds the buttons.
    JPanel buttonPanel = new JPanel();
    buttonPanel.setPreferredSize(new Dimension(196, 308));
    buttonPanel.setLayout (new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    mainPanel.add(buttonPanel, BorderLayout.LINE_START);

    Dimension minSize = new Dimension(10, 10);
    Dimension prefSize = new Dimension(10, 10);
    Dimension maxSize = new Dimension(10, 10);

    buttonPanel.add(new Box.Filler(minSize, prefSize, maxSize));

    frontButton = new JButton("Front Specs");
    frontButton.setActionCommand("show_front");
    frontButton.addActionListener(this);
    frontButton.setAlignmentX(CENTER_ALIGNMENT);
    buttonPanel.add(frontButton);

    buttonPanel.add(new Box.Filler(minSize, prefSize, maxSize));

    rearButton = new JButton("Rear Specs");
    rearButton.setActionCommand("show_rear");
    rearButton.addActionListener(this);
    rearButton.setAlignmentX(CENTER_ALIGNMENT);
    buttonPanel.add(rearButton);

    buttonPanel.add(new Box.Filler(minSize, prefSize, maxSize));

    calcButton = new JButton("Calc Lengths");
    calcButton.setActionCommand("show_calc");
    calcButton.addActionListener(this);
    calcButton.setEnabled(false);
    calcButton.setAlignmentX(CENTER_ALIGNMENT);
    buttonPanel.add(calcButton);

    buttonPanel.add(new Box.Filler(minSize, prefSize, maxSize));

    defaultMsg = new String("Input front specs and/or rear specs and press Calc Lengths.");
    msgText = new JTextArea(defaultMsg);
    msgText.setBackground(buttonPanel.getBackground());
    msgText.setLineWrap(true);
    msgText.setWrapStyleWord(true);
    msgText.setAlignmentX(CENTER_ALIGNMENT);
    Border msgBorder = BorderFactory.createEmptyBorder (10, 10, 10, 10);
    msgText.setBorder(msgBorder);
    msgText.setEditable(false);
    buttonPanel.add(msgText);

    // Set up FRONT specs panel.
    JPanel frontPanel = new JPanel();
    setUpFrontSpecsPanel(frontPanel);

    // Set up REAR specs panel.
    JPanel rearPanel = new JPanel();
    setUpRearSpecsPanel(rearPanel);

    // Set up CALC panel to hold results.
    JPanel calcPanel = new JPanel();
    setUpCalcResultsPanel(calcPanel);

    // Set up IMAGE panel.
    JPanel imagePanel = new JPanel();
    setUpImagePanel(imagePanel);

    // Set up RIGHT panel to hold FRONT, REAR, CALC, and IMAGE as alternative panels.
    rightPanel = new JPanel(new CardLayout());
    rightPanel.setPreferredSize(new Dimension(400, 304));
    rightPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    rightPanel.add(frontPanel, FRONTPANEL);
    rightPanel.add(rearPanel, REARPANEL);
    rightPanel.add(calcPanel, CALCPANEL);
    rightPanel.add(imagePanel, IMAGEPANEL);

    // Hack to force space between applet border and top of right panel border.
    // Should probably fix by using GridBagLayout manager but this is easier.
    mainPanel.add(new Box.Filler(new Dimension (1,1), new Dimension (1,1), new Dimension (1,1)), BorderLayout.PAGE_START);

    mainPanel.add(rightPanel, BorderLayout.LINE_END);

    // Get images for Help dialogs.
    try {
      String frontHelpLocation = "images/helpFront.gif";
      URL frontHelpUrl = new URL(getCodeBase(), frontHelpLocation);
      frontHelpImage = new ImageIcon(ImageIO.read(frontHelpUrl));

      String rearHelpLocation = "images/helpRear.gif";
      URL rearHelpUrl = new URL(getCodeBase(), rearHelpLocation);
      rearHelpImage = new ImageIcon(ImageIO.read(rearHelpUrl));
    }
    catch (IOException ex) {
      System.out.println("exception reading help images");
    }

    // Start with IMAGE in RIGHT panel.
    showImage();
  }

  // *******************************************************************

  public void actionPerformed(ActionEvent e) {

    if ("show_front".equals(e.getActionCommand())) {
        showFront();
    }
    else if ("save_front".equals(e.getActionCommand())) {
        saveFront();
    } 
    else if ("discard_front".equals(e.getActionCommand())) {
        discardFront();
    } 
    else if ("help_front".equals(e.getActionCommand())) {
        helpFront();
    } 
    else if ("show_rear".equals(e.getActionCommand())) {
        showRear();
    } 
    else if ("save_rear".equals(e.getActionCommand())) {
        saveRear();
    } 
    else if ("discard_rear".equals(e.getActionCommand())) {
        discardRear();
    } 
    else if ("help_rear".equals(e.getActionCommand())) {
        helpRear();
    } 
    else if ("show_calc".equals(e.getActionCommand())) {
        showCalc();
    }
    else if ("ok_calc".equals(e.getActionCommand())) {
      enableButtons();
      showImage();
    } 
    else if ("image_timer".equals(e.getActionCommand())) {
      nextImage();
    }
  }

  // *******************************************************************

  void setUpFrontSpecsPanel(JPanel frontPanel) {
    frontPanel.setLayout(new BoxLayout(frontPanel, BoxLayout.Y_AXIS));

    // Title
    JLabel frontLabel = new JLabel("Front Wheel Specifications");
    frontLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    frontLabel.setHorizontalAlignment(SwingConstants.CENTER);
    frontLabel.setBackground(Color.LIGHT_GRAY);
    frontLabel.setOpaque(true);
    frontLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    frontPanel.add(frontLabel);

    // Inputs - creating 7 rows even though only use 5 so row height will match Rear inputs.
    JPanel frontSelections = new JPanel(new GridLayout(7, 2));

    JLabel rimDiameterLabel = new JLabel("Rim Diameter: ");
    rimDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    frontSelections.add(rimDiameterLabel);
    frontRimDiameter = new JTextField(String.valueOf(frontRimDiameterValue));
    frontSelections.add(frontRimDiameter);

    JLabel hubDiameterLabel = new JLabel("Hub Diameter: ");
    hubDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    frontSelections.add(hubDiameterLabel);
    frontHubDiameter = new JTextField(String.valueOf(frontHubDiameterValue));
    frontSelections.add(frontHubDiameter);

    JLabel hubWidthLabel = new JLabel("Hub Width: ");
    hubWidthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    frontSelections.add(hubWidthLabel);
    frontHubWidth = new JTextField(String.valueOf(frontHubWidthValue));
    frontSelections.add(frontHubWidth);

    JLabel crossLabel = new JLabel("Cross: ");
    crossLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    frontSelections.add(crossLabel);
    frontCross = new JComboBox<String>(crossStrings);
    frontCross.setSelectedIndex(frontCrossValue);
    frontCross.addActionListener(this);
    frontCross.setActionCommand("cross_front");
    frontSelections.add(frontCross);

    JLabel holesLabel = new JLabel("Number Holes: ");
    holesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    frontSelections.add(holesLabel);
    frontHoles = new JComboBox<String>(holesStrings);
    frontHoles.setSelectedIndex(frontHolesValue);
    frontHoles.addActionListener(this);
    frontHoles.setActionCommand("holes_front");
    frontSelections.add(frontHoles);

    // Some dummy stuff to keep gridlayout happy.
    frontSelections.add(new JLabel());
    frontSelections.add(new JLabel());
    frontSelections.add(new JLabel());
    frontSelections.add(new JLabel());

    frontPanel.add(frontSelections);

    // Force buttons to bottom of layout.
    frontPanel.add(Box.createVerticalGlue());

    // Buttons
    JPanel frontButtonPanel = new JPanel();
    frontButtonPanel.setLayout(new BoxLayout(frontButtonPanel, BoxLayout.X_AXIS));

    JButton saveFrontButton = new JButton ("Save");
    saveFrontButton.setActionCommand("save_front");
    saveFrontButton.addActionListener(this);
    frontButtonPanel.add (saveFrontButton);

    JButton discardFrontButton = new JButton ("Discard");
    discardFrontButton.setActionCommand("discard_front");
    discardFrontButton.addActionListener(this);
    frontButtonPanel.add (discardFrontButton);

    JButton helpFrontButton = new JButton ("Help");
    helpFrontButton.setActionCommand("help_front");
    helpFrontButton.addActionListener(this);
    frontButtonPanel.add (helpFrontButton);

    frontPanel.add(frontButtonPanel);
  }


  // *******************************************************************

  void setUpRearSpecsPanel(JPanel rearPanel) {
    rearPanel.setLayout(new BoxLayout(rearPanel, BoxLayout.Y_AXIS));

    // Title
    JLabel rearLabel = new JLabel("rear Wheel Specifications");
    rearLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    rearLabel.setHorizontalAlignment(SwingConstants.CENTER);
    rearLabel.setBackground(Color.LIGHT_GRAY);
    rearLabel.setOpaque(true);
    rearLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    rearPanel.add(rearLabel);

    // Inputs
    JPanel rearSelections = new JPanel(new GridLayout(7, 2));

    JLabel rimDiameterLabel = new JLabel("Rim Diameter: ");
    rimDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    rearSelections.add(rimDiameterLabel);
    rearRimDiameter = new JTextField(String.valueOf(rearRimDiameterValue));
    rearSelections.add(rearRimDiameter);

    JLabel hubDiameterLabel = new JLabel("Hub Diameter: ");
    hubDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    rearSelections.add(hubDiameterLabel);
    rearHubDiameter = new JTextField(String.valueOf(rearHubDiameterValue));
    rearSelections.add(rearHubDiameter);

    JLabel hubWidthLabel = new JLabel("Hub Width: ");
    hubWidthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    rearSelections.add(hubWidthLabel);
    rearHubWidth = new JTextField(String.valueOf(rearHubWidthValue));
    rearSelections.add(rearHubWidth);

    JLabel leftOffsetLabel = new JLabel("Left Offset: ");
    leftOffsetLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    rearSelections.add(leftOffsetLabel);
    rearLeftOffset = new JTextField(String.valueOf(rearLeftOffsetValue));
    rearSelections.add(rearLeftOffset);

    JLabel rightOffsetLabel = new JLabel("Right Offset: ");
    rightOffsetLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    rearSelections.add(rightOffsetLabel);
    rearRightOffset = new JTextField(String.valueOf(rearRightOffsetValue));
    rearSelections.add(rearRightOffset);

    JLabel crossLabel = new JLabel("Cross: ");
    crossLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    rearSelections.add(crossLabel);
    rearCross = new JComboBox<String>(crossStrings);
    rearCross.setSelectedIndex(rearCrossValue);
    rearCross.addActionListener(this);
    rearCross.setActionCommand("cross_rear");
    rearSelections.add(rearCross);

    JLabel holesLabel = new JLabel("Number Holes: ");
    holesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    rearSelections.add(holesLabel);
    rearHoles = new JComboBox<String>(holesStrings);
    rearHoles.setSelectedIndex(rearHolesValue);
    rearHoles.addActionListener(this);
    rearHoles.setActionCommand("holes_rear");
    rearSelections.add(rearHoles);

    rearPanel.add(rearSelections);

    // Force buttons to bottom of layout.
    rearPanel.add(Box.createVerticalGlue());

    // Buttons
    JPanel rearButtonPanel = new JPanel();
    rearButtonPanel.setLayout(new BoxLayout(rearButtonPanel, BoxLayout.X_AXIS));

    JButton saverearButton = new JButton ("Save");
    saverearButton.setActionCommand("save_rear");
    saverearButton.addActionListener(this);
    rearButtonPanel.add (saverearButton);

    JButton discardrearButton = new JButton ("Discard");
    discardrearButton.setActionCommand("discard_rear");
    discardrearButton.addActionListener(this);
    rearButtonPanel.add (discardrearButton);

    JButton helprearButton = new JButton ("Help");
    helprearButton.setActionCommand("help_rear");
    helprearButton.addActionListener(this);
    rearButtonPanel.add (helprearButton);

    rearPanel.add(rearButtonPanel);
  }


  // *******************************************************************

  void setUpCalcResultsPanel(JPanel calcPanel) {
    calcPanel.setLayout(new BoxLayout(calcPanel, BoxLayout.Y_AXIS));

    // Title
    JLabel calcLabel = new JLabel("Calculated Spoke Lengths");
    calcLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    calcLabel.setHorizontalAlignment(SwingConstants.CENTER);
    calcLabel.setBackground(Color.LIGHT_GRAY);
    calcLabel.setOpaque(true);
    calcLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    calcPanel.add(calcLabel);

    // Results - creating 7 rows even though only use 3 so row height will match inputs.
    JPanel calcResults = new JPanel(new GridLayout(7, 2));

    // Front wheel
    JLabel frontLengthLabel = new JLabel("Front Length: ");
    frontLengthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    calcResults.add(frontLengthLabel);
    frontLength = new JTextField();
    calcResults.add(frontLength);

    JLabel leftRearLengthLabel = new JLabel("Left Rear Length: ");
    leftRearLengthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    calcResults.add(leftRearLengthLabel);
    leftRearLength = new JTextField();
    calcResults.add(leftRearLength);

    JLabel rightRearLengthLabel = new JLabel("Right Rear Length: ");
    rightRearLengthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    calcResults.add(rightRearLengthLabel);
    rightRearLength = new JTextField();
    calcResults.add(rightRearLength);

    // Some dummy stuff to keep gridlayout happy.
    calcResults.add(new JLabel());
    calcResults.add(new JLabel());
    calcResults.add(new JLabel());
    calcResults.add(new JLabel());
    calcResults.add(new JLabel());
    calcResults.add(new JLabel());
    calcResults.add(new JLabel());
    calcResults.add(new JLabel());

    calcPanel.add(calcResults);

    // Force buttons to bottom of layout.
    calcPanel.add(Box.createVerticalGlue());

    JButton saveCalcButton = new JButton ("OK");
    saveCalcButton.setActionCommand("ok_calc");
    saveCalcButton.addActionListener(this);
    calcPanel.add (saveCalcButton);  
  }


  // *******************************************************************

  void setUpImagePanel(JPanel imagePanel) {
    imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));

    imageLabel = new JLabel();
    for (int i = 0 ; i < NBR_IMAGES; i++) { 
      try {
        String location = "images/spokeLength" + i + "0.gif";
        URL url = new URL(getCodeBase(), location);
        images[i] = ImageIO.read(url);
      }
      catch (IOException ex) {
          imageLabel.setText("Error:");
      }
    }

    imageNbr = 0;
    imageLabel.setIcon(new ImageIcon(images[imageNbr]));
    imageLabel.setAlignmentX(CENTER_ALIGNMENT);
    imagePanel.add (imageLabel);

    int toDuration = 100; // milliseconds
    int toInitialDelay = 100;
    imageTimer = new Timer(toDuration, this);
    imageTimer.setInitialDelay(toInitialDelay);
    imageTimer.setActionCommand("image_timer");
  }

  // *******************************************************************

  void showImage() {
    enableButtons();
    CardLayout cl = (CardLayout)(rightPanel.getLayout());
    cl.show(rightPanel, IMAGEPANEL);

    imageTimer.start();
  }

  // *******************************************************************

  void nextImage() {
    // Show images in reverse order for CCW rotation.
    if (imageNbr == 0)
    {
      imageNbr = NBR_IMAGES;
    }

    imageNbr--;

    imageLabel.setIcon(new ImageIcon(images[imageNbr]));
  }

  // *******************************************************************

  void showFront() {
    disableButtons();

    imageTimer.stop();

    CardLayout cl = (CardLayout)(rightPanel.getLayout());
    cl.show(rightPanel, FRONTPANEL);
  }

  // *******************************************************************

  void saveFront() {
    boolean inputGood = true;

    // Process rim diameter.
    double tempRimDiameter = 0.0;
    try {
      tempRimDiameter = Double.parseDouble(frontRimDiameter.getText());

      if (tempRimDiameter == 0.0) {
        frontRimDiameter.setBackground(LIGHT_RED);
        inputGood = false;
      }
      else {
        frontRimDiameter.setBackground(TEXTFIELD_COLOR);
      }
    }
    catch (NumberFormatException ex) {
      System.out.println("exception getting front rim diameter");
      frontRimDiameter.setBackground(LIGHT_RED);
      inputGood = false;
    }

    // Process hub diameter.
    double tempHubDiameter = 0.0;
    try {
      tempHubDiameter = Double.parseDouble(frontHubDiameter.getText());

      if (tempHubDiameter == 0.0) {
        frontHubDiameter.setBackground(LIGHT_RED);
        inputGood = false;
      }
      else {
        frontHubDiameter.setBackground(TEXTFIELD_COLOR);
      }
    }
    catch (NumberFormatException ex) {
      System.out.println("exception getting front hub diameter");
      frontHubDiameter.setBackground(LIGHT_RED);
      inputGood = false;
    }

    // Process hub width.
    double tempHubWidth = 0.0;
    try {
      tempHubWidth = Double.parseDouble(frontHubWidth.getText());

      if (tempHubWidth == 0.0) {
        frontHubWidth.setBackground(LIGHT_RED);
        inputGood = false;
      }
      else {
        frontHubWidth.setBackground(TEXTFIELD_COLOR);
      }
    }
    catch (NumberFormatException ex) {
      System.out.println("exception getting front hub width");
      frontHubWidth.setBackground(LIGHT_RED);
      inputGood = false;
    }

    // Process cross.
    int tempCross = frontCross.getSelectedIndex();
    if (tempCross == 0) {
      frontCross.setBackground(LIGHT_RED);
      inputGood = false;
    }
    else {
      frontCross.setBackground(COMBO_COLOR);
    }

    // Process holes.
    int tempHoles = frontHoles.getSelectedIndex();
    if (tempHoles == 0) {
      frontHoles.setBackground(LIGHT_RED);
      inputGood = false;
    }
    else {
      frontHoles.setBackground(COMBO_COLOR);
    }

    // Sanity check
    if (inputGood) {

      msgText.setText(defaultMsg);

      frontRimDiameterValue = tempRimDiameter;
      frontHubDiameterValue = tempHubDiameter;
      frontHubWidthValue = tempHubWidth;
      frontCrossValue = tempCross;
      frontHolesValue = tempHoles;

      enableButtons();
      showImage();
    }
    else {
      msgText.setText("Error in input values.");
    }
  }

  // *******************************************************************

  void discardFront() {

    msgText.setText(defaultMsg);

    // Restore last saved values.
    frontRimDiameter.setText(String.valueOf(frontRimDiameterValue));
    frontRimDiameter.setBackground(TEXTFIELD_COLOR);
    frontHubDiameter.setText(String.valueOf(frontHubDiameterValue));
    frontHubDiameter.setBackground(TEXTFIELD_COLOR);
    frontHubWidth.setText(String.valueOf(frontHubWidthValue));
    frontHubWidth.setBackground(TEXTFIELD_COLOR);
    frontCross.setSelectedIndex(frontCrossValue);
    frontCross.setBackground(COMBO_COLOR);
    frontHoles.setSelectedIndex(frontHolesValue);
    frontHoles.setBackground(COMBO_COLOR);

    enableButtons();
    showImage();
  }

  // *******************************************************************

  void helpFront() {
    JOptionPane.showMessageDialog(null,
                                  "", 
                                  "Front Help",
                                  JOptionPane.INFORMATION_MESSAGE,
                                  frontHelpImage);
  }

  // *******************************************************************

  void showRear() {
    disableButtons();

    imageTimer.stop();
    
    CardLayout cl = (CardLayout)(rightPanel.getLayout());
    cl.show(rightPanel, REARPANEL);
  }

  // *******************************************************************

  void saveRear() {
    boolean inputGood = true;

    // Process rim diameter.
    double tempRimDiameter = 0.0;
    try {
      tempRimDiameter = Double.parseDouble(rearRimDiameter.getText());

      if (tempRimDiameter == 0.0) {
        rearRimDiameter.setBackground(LIGHT_RED);
        inputGood = false;
      }
      else {
        rearRimDiameter.setBackground(TEXTFIELD_COLOR);
      }
    }
    catch (NumberFormatException ex) {
      System.out.println("exception getting rear rim diameter");
      rearRimDiameter.setBackground(LIGHT_RED);
      inputGood = false;
    }

    // Process hub diameter.
    double tempHubDiameter = 0.0;
    try {
      tempHubDiameter = Double.parseDouble(rearHubDiameter.getText());

      if (tempHubDiameter == 0.0) {
        rearHubDiameter.setBackground(LIGHT_RED);
        inputGood = false;
      }
      else {
        rearHubDiameter.setBackground(TEXTFIELD_COLOR);
      }
    }
    catch (NumberFormatException ex) {
      System.out.println("exception getting rear hub diameter");
      rearHubDiameter.setBackground(LIGHT_RED);
      inputGood = false;
    }

    // Process hub width.
    double tempHubWidth = 0.0;
    try {
      tempHubWidth = Double.parseDouble(rearHubWidth.getText());

      if (tempHubWidth == 0.0) {
        rearHubWidth.setBackground(LIGHT_RED);
        inputGood = false;
      }
      else {
        rearHubWidth.setBackground(TEXTFIELD_COLOR);
      }
    }
    catch (NumberFormatException ex) {
      System.out.println("exception getting rear hub width");
      rearHubWidth.setBackground(LIGHT_RED);
      inputGood = false;
    }

    // Process left offset.
    double tempLeftOffset = 0.0;
    try {
      tempLeftOffset = Double.parseDouble(rearLeftOffset.getText());

      if (tempLeftOffset == 0.0) {
        rearLeftOffset.setBackground(LIGHT_RED);
        inputGood = false;
      }
      else {
        rearLeftOffset.setBackground(TEXTFIELD_COLOR);
      }
    }
    catch (NumberFormatException ex) {
      System.out.println("exception getting rear left offset");
      rearLeftOffset.setBackground(LIGHT_RED);
      inputGood = false;
    }

    // Process right offset.
    double tempRightOffset = 0.0;
    try {
      tempRightOffset = Double.parseDouble(rearRightOffset.getText());

      if (tempRightOffset == 0.0) {
        rearRightOffset.setBackground(LIGHT_RED);
        inputGood = false;
      }
      else {
        rearRightOffset.setBackground(TEXTFIELD_COLOR);
      }
    }
    catch (NumberFormatException ex) {
      System.out.println("exception getting rear right offset");
      rearRightOffset.setBackground(LIGHT_RED);
      inputGood = false;
    }

    // Process cross.
    int tempCross = rearCross.getSelectedIndex();
    if (tempCross == 0) {
      rearCross.setBackground(LIGHT_RED);
      inputGood = false;
    }
    else {
      rearCross.setBackground(COMBO_COLOR);
    }

    // Process holes.
    int tempHoles = rearHoles.getSelectedIndex();
    if (tempHoles == 0) {
      rearHoles.setBackground(LIGHT_RED);
      inputGood = false;
    }
    else {
      rearHoles.setBackground(COMBO_COLOR);
    }

    // Sanity check
    if (inputGood) {

      msgText.setText(defaultMsg);

      rearRimDiameterValue = tempRimDiameter;
      rearHubDiameterValue = tempHubDiameter;
      rearHubWidthValue = tempHubWidth;
      rearLeftOffsetValue = tempLeftOffset;
      rearRightOffsetValue = tempRightOffset;
      rearCrossValue = tempCross;
      rearHolesValue = tempHoles;

      enableButtons();
      showImage();
    }
    else {
      msgText.setText("Error in input values.");
    }
  }

  // *******************************************************************

  void discardRear() {

    msgText.setText(defaultMsg);

    // Restore last saved values.
    rearRimDiameter.setText(String.valueOf(rearRimDiameterValue));
    rearRimDiameter.setBackground(TEXTFIELD_COLOR);
    rearHubDiameter.setText(String.valueOf(rearHubDiameterValue));
    rearHubDiameter.setBackground(TEXTFIELD_COLOR);
    rearHubWidth.setText(String.valueOf(rearHubWidthValue));
    rearHubWidth.setBackground(TEXTFIELD_COLOR);
    rearLeftOffset.setText(String.valueOf(rearLeftOffsetValue));
    rearLeftOffset.setBackground(TEXTFIELD_COLOR);
    rearRightOffset.setText(String.valueOf(rearRightOffsetValue));
    rearRightOffset.setBackground(TEXTFIELD_COLOR);
    rearCross.setSelectedIndex(rearCrossValue);
    rearCross.setBackground(COMBO_COLOR);
    rearHoles.setSelectedIndex(rearHolesValue);
    rearHoles.setBackground(COMBO_COLOR);

    enableButtons();
    showImage();
  }

  // *******************************************************************

  void helpRear() {
    JOptionPane.showMessageDialog(null,
                                  "", 
                                  "Rear Help",
                                  JOptionPane.INFORMATION_MESSAGE,
                                  rearHelpImage);
  }

  // *******************************************************************

  void showCalc() {
    disableButtons();

    imageTimer.stop();

    String lengthValue;

    // Front wheel.
    if (frontRimDiameterValue != 0.0)
    {
      lengthValue = lengthMath (frontRimDiameterValue/2.0,
                                frontHubDiameterValue/2.0,
                                frontHubWidthValue/2.0,
                                frontCrossValue,
                                frontHolesValue);
      frontLength.setText(lengthValue);
      frontLength.setForeground(Color.BLACK);
    }
    else
    {
      frontLength.setText("Missing inputs");
      frontLength.setForeground(LIGHT_RED);
    }

    // Rear wheel.
    if (rearRimDiameterValue != 0.0)
    {
      lengthValue = lengthMath (rearRimDiameterValue/2.0,
                                rearHubDiameterValue/2.0,
                                (rearHubWidthValue + rearRightOffsetValue - rearLeftOffsetValue)/2.0,
                                rearCrossValue,
                                rearHolesValue);
      leftRearLength.setText(lengthValue);
      leftRearLength.setForeground(Color.BLACK);

      lengthValue = lengthMath (rearRimDiameterValue/2.0,
                                rearHubDiameterValue/2.0,
                                (rearHubWidthValue + rearLeftOffsetValue - rearRightOffsetValue)/2.0,
                                rearCrossValue,
                                rearHolesValue);
      rightRearLength.setText(lengthValue);
      rightRearLength.setForeground(Color.BLACK);
    }
    else
    {
      leftRearLength.setText("Missing inputs");
      leftRearLength.setForeground(LIGHT_RED);

      rightRearLength.setText("Missing inputs");
      rightRearLength.setForeground(LIGHT_RED);
    }
    
    CardLayout cl = (CardLayout)(rightPanel.getLayout());
    cl.show(rightPanel, CALCPANEL);
  }

  // *******************************************************************

  void enableButtons() {
    frontButton.setEnabled(true);
    rearButton.setEnabled(true);

    // Enable calculation only if representative value indicates a successful
    // save of specifications.
    if (frontRimDiameterValue != 0.0 ||
        frontRimDiameterValue != 0.0) {
      calcButton.setEnabled(true);
    }
  }

  // *******************************************************************

  void disableButtons() {
    frontButton.setEnabled(false);
    rearButton.setEnabled(false);
    calcButton.setEnabled(false);
  }


  // *******************************************************************

  // Do math to actually determine spoke length.
  String lengthMath (double rimRadius, double hubRadius, double hubDepth, double cross, double holes) {

    double twoPi = 2 * 3.14159;
    double centralPlaneAngle = (cross * 2.0 * twoPi)/holes;
    double centralPlaneProj = Math.pow(((hubRadius * hubRadius) +
                                          (rimRadius * rimRadius) -
                                          (2.0 * hubRadius * rimRadius *
                                            Math.cos (centralPlaneAngle))),
                                       0.5);

    double length = Math.pow(((centralPlaneProj * centralPlaneProj) +
                                (hubDepth * hubDepth)),
                             0.5);

    // Limit precision to 3 decimal places.
    DecimalFormat df = new DecimalFormat("#.###");
    df.setRoundingMode(RoundingMode.HALF_UP);
    String retVal = new String(df.format(length));
    
    return retVal;
  }
}
