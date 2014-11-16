package setec.g3.ui;

import java.util.Calendar;

import setec.g3.ui.view.viewgroup.FlyOutContainer;
import setec.g3.ui.view.viewgroup.FlyOutContainer.MessageItemPriority;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.content.Context;
import android.content.res.Resources;
import android.widget.TextView;

import setec.g3.ui.R;

public class MainUI extends Activity implements OnTouchListener {

	/* component handling variables */
	private FlyOutContainer root;
	private Button backFromLineOfFireBtn, backFromSettingsBtn, backFromPreDefinedMessageBtn;
	
	/* movement handling */
	private enum actioDialStateEnum{ STILL, START, MOVING, RETURNING; }
	private actioDialStateEnum actioDialCurrentState=actioDialStateEnum.RETURNING;
	private float actionDialBaseX,actionDialBaseY=0.0f;
	private float x,y=0.0f;
	protected Runnable dialAnimationRunnable = new DialAnimationRunnable();
	protected Handler dialAnimationHandler = new Handler();
	private static final int dialAnimationPollingInterval = 15;
	private static final int dialReturningAnimationDuration = 200;
	private static final int dialMovementMaxRadius = 250;
	private static final long dialAnimationInDuration = 300;

	/* action dial */
	private ImageView actionDial;
	private float actionDialWidth,actionDialHeight;
	
	/* dial base */
	private ImageView actionDialCircle;
	private float actionDialCircleWidth,actionDialCircleHeight;
	private float actionDialCircleBaseX,actionDialCircleBaseY;
	private float distanceFromCenter;
	private float iconSnapRadius;
	
	/* dial line of fire icon (N) */
	private ImageView lineOfFireIcon;
	private float lineOfFireIconWidth, lineOfFireIconHeight;
	private float lineOfFireIconBaseX, lineOfFireIconBaseY;
	
	/* dial settings icon (S) */
	private ImageView settingsIcon;
	private float settingsIconWidth, settingsIconHeight;
	private float settingsIconBaseX, settingsIconBaseY;
	
	/* dial messages icon (E) */
	private ImageView messagesIcon;
	private float messagesIconWidth, messagesIconHeight;
	private float messagesIconBaseX, messagesIconBaseY;
	
	/* right messaging view stuff */
	private Button prioritySelector, sendMessage;
	private enum PriorityLevel {NORMAL, NORMAL_PLUS, IMPORTANT, CRITICAL};
	private PriorityLevel currentPriorityLevel=PriorityLevel.NORMAL;
	private EditText messageTextBox;
	
	/* dial pre defined messages icon (W) */
	private ImageView preDefMessagesIcon;
	private float preDefMessagesIconWidth, preDefMessagesIconHeight;
	private float preDefMessagesIconBaseX, preDefMessagesIconBaseY;
	
	/* selection state */
	private enum dialDisplayState { OFF, OPENING, ON, CLOSING };
	private dialDisplayState dialState = dialDisplayState.OFF; 
	private enum dialSelectionSate { UNSELECTED, LINE_OF_FIRE, MESSAGE, SETTINGS, PRE_DEFINED_MESSAGES };
	private dialSelectionSate dialSelection = dialSelectionSate.UNSELECTED;
	
	/* flow buttons */
	private Button backToMainBtn, backToLineOfFireBtn, backToMessageBtn, backToSettingsBtn, backToPreDefinedMessageBtn;
	private float backBtnX, backBtnY;
	private float backToOption1BtnX, backToOption1BtnY;
	private float backToOption2BtnX, backToOption2BtnY;
	private float backToOption3BtnX, backToOption3BtnY;
	
	/* Dial Text */
	private TextView dialText;
	private StringBuilder dialTextBuilder;
	float dialTextBaseX, dialTextBaseY;
	float dialTextWidth, dialTextHeight;
	
	/* 
	 * Upon creation do:
	 * 		1- inflate main_ui.xml (convert from xml into java object)
	 * 		2- put it as root view
	 * 		3- attach its components to handlers
	 * 		4- add the components listeners
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.root = (FlyOutContainer) this.getLayoutInflater().inflate(R.layout.main_ui, null);
		
		this.setContentView(root);
		
		initializeUI();
	}

	public void initializeUI(){
		attachComponents();
		addInterfaceListeners();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sample, menu);
		return true;
	}
	
	public float[] getCenterOfScreen(){
		float center[]={0,0};
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		center[0]=(float)displaymetrics.widthPixels/2.0f;
		center[1]=(float)displaymetrics.heightPixels/2.0f;
		return center;
	}
	
	public float[] getScreenSize(){
		float widthAndHeight[]={0,0};
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		widthAndHeight[0]=(float)displaymetrics.widthPixels;
		widthAndHeight[1]=(float)root.getHeight();/*(float)displaymetrics.heightPixels;*/
		return widthAndHeight;
	}
	
	public void prepareActionDial(){
		float center[]=getCenterOfScreen();
		distanceFromCenter=250;
		iconSnapRadius=100;
		
		/* action dial */
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    params.addRule(RelativeLayout.CENTER_VERTICAL);
	    params.height = 160;
	    params.width = 160;
	    actionDial.setLayoutParams(params);
		actionDial.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		actionDial.setImageResource(R.drawable.dial_off);
		actionDialWidth=actionDial.getLayoutParams().width;
		actionDialHeight=actionDial.getLayoutParams().height;
		actionDialBaseX = center[0] - actionDialWidth/2.0f;
		actionDialBaseY = center[1] - actionDialHeight/2.0f;
		x=actionDialBaseX;
		y=actionDialBaseY;
		
		/* dial base */
		RelativeLayout.LayoutParams paramsDialer = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsDialer.addRule(RelativeLayout.CENTER_HORIZONTAL);
		paramsDialer.addRule(RelativeLayout.CENTER_VERTICAL);
		paramsDialer.height = 500;
		paramsDialer.width = 500;
	    actionDialCircle.setLayoutParams(paramsDialer);
	    actionDialCircle.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	    actionDialCircle.setImageResource(R.drawable.dialer_base);
	    actionDialCircleWidth=actionDialCircle.getLayoutParams().width;
		actionDialCircleHeight=actionDialCircle.getLayoutParams().height;
		actionDialCircleBaseX = center[0] - actionDialCircleWidth/2.0f;
		actionDialCircleBaseY = center[1] - actionDialCircleHeight/2.0f;
		actionDialCircle.setVisibility(View.INVISIBLE);
		
		/* icons parameters and Images */
		RelativeLayout.LayoutParams paramsDialerIcons = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsDialerIcons.height = 100;
		paramsDialerIcons.width = 100;
		lineOfFireIcon.setLayoutParams(paramsDialerIcons);
		lineOfFireIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		lineOfFireIcon.setImageResource(R.drawable.option_line_fire);
		lineOfFireIcon.setVisibility(View.INVISIBLE);
		
		messagesIcon.setLayoutParams(paramsDialerIcons);
		messagesIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		messagesIcon.setImageResource(R.drawable.option_message);
		messagesIcon.setVisibility(View.INVISIBLE);
		
		settingsIcon.setLayoutParams(paramsDialerIcons);
		settingsIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		settingsIcon.setImageResource(R.drawable.option_settings);
		settingsIcon.setVisibility(View.INVISIBLE);
		
		preDefMessagesIcon.setLayoutParams(paramsDialerIcons);
		preDefMessagesIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		preDefMessagesIcon.setImageResource(R.drawable.option_def_message);
		preDefMessagesIcon.setVisibility(View.INVISIBLE);
		
		/* flow buttons parameters */
		RelativeLayout.LayoutParams paramsFlowIcons = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsFlowIcons.height = 100;
		paramsFlowIcons.width = 100;
		backToMainBtn.setLayoutParams(paramsFlowIcons);
		backToMainBtn.setVisibility(View.INVISIBLE);
		
		backToLineOfFireBtn.setLayoutParams(paramsFlowIcons);
		backToLineOfFireBtn.setVisibility(View.INVISIBLE);
		
		backToSettingsBtn.setLayoutParams(paramsFlowIcons);
		backToSettingsBtn.setVisibility(View.INVISIBLE);
		
		backToPreDefinedMessageBtn.setLayoutParams(paramsFlowIcons);
		backToPreDefinedMessageBtn.setVisibility(View.INVISIBLE);
		
		backToMessageBtn.setLayoutParams(paramsFlowIcons);
		backToMessageBtn.setVisibility(View.INVISIBLE);
		
		backBtnX=0;
		backBtnY=0;
		backToOption1BtnX=0;
		backToOption1BtnY=0;
		backToOption2BtnX=0;
		backToOption2BtnY=0;
		backToOption3BtnX=0;
		backToOption3BtnY=0;
		
		/* right message view */
		prioritySelector.setLayoutParams(paramsFlowIcons);
		prioritySelector.setVisibility(View.INVISIBLE);
		//sendMessage.setLayoutParams(paramsFlowIcons);
		
		/* dial text parameters */
		RelativeLayout.LayoutParams paramsDialerText = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsDialerText.height = 400;
		paramsDialerText.width = 400;
		dialText.setLayoutParams(paramsDialerText);
		dialText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		dialTextBaseX=center[0] - (paramsDialerText.width/2);
		dialTextBaseY=center[1] - (paramsDialerText.height/2);
		dialTextWidth=paramsDialerText.width;
		dialTextHeight=paramsDialerText.height;
		dialText.setVisibility(View.INVISIBLE);
		
		/* icons positions, widths and height */
		lineOfFireIconBaseX=actionDialBaseX + ( (params.width-paramsDialerIcons.width) / 2 );
		lineOfFireIconBaseY=actionDialBaseY-distanceFromCenter + ( (params.height-paramsDialerIcons.height) / 2 );
		lineOfFireIconWidth=paramsDialerIcons.width;
		lineOfFireIconHeight=paramsDialerIcons.height;
		
		settingsIconBaseX=actionDialBaseX + ( (params.width-paramsDialerIcons.width) / 2 );
		settingsIconBaseY=actionDialBaseY+distanceFromCenter + ( (params.height-paramsDialerIcons.height) / 2 );
		settingsIconWidth=paramsDialerIcons.width;
		settingsIconHeight=paramsDialerIcons.height;
		
		messagesIconBaseX=actionDialBaseX+distanceFromCenter + ( (params.width-paramsDialerIcons.width) / 2 );
		messagesIconBaseY=actionDialBaseY + ( (params.height-paramsDialerIcons.height) / 2 );
		messagesIconWidth=paramsDialerIcons.width;
		messagesIconHeight=paramsDialerIcons.height;
		
		preDefMessagesIconBaseX=actionDialBaseX-distanceFromCenter + ( (params.width-paramsDialerIcons.width) / 2 );
		preDefMessagesIconBaseY=actionDialBaseY + ( (params.height-paramsDialerIcons.height) / 2 );
		preDefMessagesIconWidth=paramsDialerIcons.width;
		preDefMessagesIconHeight=paramsDialerIcons.height;

		/* ordering on the z axis */
		actionDial.bringToFront();
		lineOfFireIcon.bringToFront();
		messagesIcon.bringToFront();
		settingsIcon.bringToFront();
		preDefMessagesIcon.bringToFront();	
		dialText.bringToFront();
	}
	
	public void resetActionDialPosition(){
		actionDial.setX( actionDialBaseX );
		actionDial.setY( actionDialBaseY );
	}
	
	public void updateSideButtonsPositionAndVisibility(){
		float widthAndHeight[] = getScreenSize();
		float minimumBorderSpace = ( (float) this.root.getSideMargin() - lineOfFireIconWidth ) / 2.0f;
		switch (dialSelection){
		case LINE_OF_FIRE:
			backBtnX=minimumBorderSpace;
			backBtnY=minimumBorderSpace;
			
			backToOption1BtnX=backBtnX + lineOfFireIconWidth + minimumBorderSpace*2;
			backToOption1BtnY=backBtnY;
			
			backToOption2BtnX=backToOption1BtnX + lineOfFireIconWidth + minimumBorderSpace/2;
			backToOption2BtnY=backBtnY;
			
			backToOption3BtnX=backToOption2BtnX + lineOfFireIconWidth + minimumBorderSpace/2;
			backToOption3BtnY=backBtnY;
			
			backToMainBtn.setX(backBtnX);
			backToMainBtn.setY(backBtnY);
			
			backToMessageBtn.setX(backToOption1BtnX);
			backToMessageBtn.setY(backToOption1BtnY);
			
			backToPreDefinedMessageBtn.setX(backToOption2BtnX);
			backToPreDefinedMessageBtn.setY(backToOption2BtnY);
			
			backToSettingsBtn.setX(backToOption3BtnX);
			backToSettingsBtn.setY(backToOption3BtnY);
			
			backToMainBtn.setVisibility(View.VISIBLE);
			/*backToLineOfFireBtn.setVisibility(View.VISIBLE);*/
			backToSettingsBtn.setVisibility(View.VISIBLE);
			backToPreDefinedMessageBtn.setVisibility(View.VISIBLE);
			backToMessageBtn.setVisibility(View.VISIBLE);
			break;
		case MESSAGE:
			backBtnX=widthAndHeight[0]-minimumBorderSpace-lineOfFireIconWidth;
			backBtnY=minimumBorderSpace;
			
			backToOption1BtnX=backBtnX;
			backToOption1BtnY=backBtnY + lineOfFireIconHeight + minimumBorderSpace*2;
			
			backToOption2BtnX=backBtnX;
			backToOption2BtnY=backToOption1BtnY + lineOfFireIconHeight + minimumBorderSpace/2;
			
			backToOption3BtnX=backBtnX;
			backToOption3BtnY=backToOption2BtnY + lineOfFireIconHeight + minimumBorderSpace/2;
			
			backToMainBtn.setX(backBtnX);
			backToMainBtn.setY(backBtnY);
			
			backToLineOfFireBtn.setX(backToOption1BtnX);
			backToLineOfFireBtn.setY(backToOption1BtnY);
			
			backToPreDefinedMessageBtn.setX(backToOption2BtnX);
			backToPreDefinedMessageBtn.setY(backToOption2BtnY);
			
			backToSettingsBtn.setX(backToOption3BtnX);
			backToSettingsBtn.setY(backToOption3BtnY);
			
			/* priority selector */
			prioritySelector.setX(backBtnX);
			prioritySelector.setY(backToOption3BtnY + lineOfFireIconHeight + minimumBorderSpace/2);
			
			backToMainBtn.setVisibility(View.VISIBLE);
			backToLineOfFireBtn.setVisibility(View.VISIBLE);
			backToSettingsBtn.setVisibility(View.VISIBLE);
			backToPreDefinedMessageBtn.setVisibility(View.VISIBLE);
			/*backToMessageBtn.setVisibility(View.VISIBLE);*/
			prioritySelector.setVisibility(View.VISIBLE);
			break;
		case SETTINGS:
			backBtnX=minimumBorderSpace;
			backBtnY=widthAndHeight[1]-minimumBorderSpace-lineOfFireIconHeight;
			
			backToOption1BtnX=backBtnX + lineOfFireIconWidth + minimumBorderSpace*2;
			backToOption1BtnY=backBtnY;
			
			backToOption2BtnX=backToOption1BtnX + lineOfFireIconWidth + minimumBorderSpace/2;
			backToOption2BtnY=backBtnY;
			
			backToOption3BtnX=backToOption2BtnX + lineOfFireIconWidth + minimumBorderSpace/2;
			backToOption3BtnY=backBtnY;
			
			backToMainBtn.setX(backBtnX);
			backToMainBtn.setY(backBtnY);
			
			backToLineOfFireBtn.setX(backToOption1BtnX);
			backToLineOfFireBtn.setY(backToOption1BtnY);
			
			backToMessageBtn.setX(backToOption2BtnX);
			backToMessageBtn.setY(backToOption2BtnY);
			
			backToPreDefinedMessageBtn.setX(backToOption3BtnX);
			backToPreDefinedMessageBtn.setY(backToOption3BtnY);
			
			backToMainBtn.setVisibility(View.VISIBLE);
			backToLineOfFireBtn.setVisibility(View.VISIBLE);
			/*backToSettingsBtn.setVisibility(View.VISIBLE);*/
			backToPreDefinedMessageBtn.setVisibility(View.VISIBLE);
			backToMessageBtn.setVisibility(View.VISIBLE);
			break;
		case PRE_DEFINED_MESSAGES:
			backBtnX=minimumBorderSpace;
			backBtnY=minimumBorderSpace;
			
			backToOption1BtnX=backBtnX;
			backToOption1BtnY=backBtnY + lineOfFireIconHeight + minimumBorderSpace*2;
			
			backToOption2BtnX=backBtnX;
			backToOption2BtnY=backToOption1BtnY + lineOfFireIconHeight + minimumBorderSpace/2;
			
			backToOption3BtnX=backBtnX;
			backToOption3BtnY=backToOption2BtnY + lineOfFireIconHeight + minimumBorderSpace/2;
			
			backToMainBtn.setX(backBtnX);
			backToMainBtn.setY(backBtnY);
			
			backToLineOfFireBtn.setX(backToOption1BtnX);
			backToLineOfFireBtn.setY(backToOption1BtnY);
			
			backToMessageBtn.setX(backToOption2BtnX);
			backToMessageBtn.setY(backToOption2BtnY);
			
			backToSettingsBtn.setX(backToOption3BtnX);
			backToSettingsBtn.setY(backToOption3BtnY);
			
			backToMainBtn.setVisibility(View.VISIBLE);
			backToLineOfFireBtn.setVisibility(View.VISIBLE);
			backToSettingsBtn.setVisibility(View.VISIBLE);
			/*backToPreDefinedMessageBtn.setVisibility(View.VISIBLE);*/
			backToMessageBtn.setVisibility(View.VISIBLE);
			break;
		case UNSELECTED:
			backToMainBtn.setVisibility(View.INVISIBLE);
			backToLineOfFireBtn.setVisibility(View.INVISIBLE);
			backToSettingsBtn.setVisibility(View.INVISIBLE);
			backToPreDefinedMessageBtn.setVisibility(View.INVISIBLE);
			backToMessageBtn.setVisibility(View.INVISIBLE);
			prioritySelector.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
	}
	
	 /* Used to fetch the components from xml */	 
	public void attachComponents(){
		backFromLineOfFireBtn = (Button) findViewById(R.id.btn_back_from_line_of_fire);
		backFromSettingsBtn = (Button) findViewById(R.id.btn_back_from_settings);
		backFromPreDefinedMessageBtn = (Button) findViewById(R.id.btn_back_from_pre_defined_messages);
		dialText = (TextView)  findViewById(R.id.main_text);
		

		actionDial = (ImageView) findViewById(R.id.action_dial_iv);
		actionDialCircle = (ImageView) findViewById(R.id.action_dial_circle_iv);
		lineOfFireIcon = (ImageView) findViewById(R.id.line_of_fire_iv);
		messagesIcon = (ImageView) findViewById(R.id.messages_iv);
		settingsIcon = (ImageView) findViewById(R.id.settings_iv);
		preDefMessagesIcon = (ImageView) findViewById(R.id.pre_defined_messages_iv);
		backToMainBtn = (Button) findViewById(R.id.back_selector);
		backToLineOfFireBtn = (Button) findViewById(R.id.line_of_fire_selector);
		backToSettingsBtn = (Button) findViewById(R.id.settings_selector);
		backToPreDefinedMessageBtn = (Button) findViewById(R.id.pre_defined_message_selector);
		backToMessageBtn = (Button) findViewById(R.id.message_selector);
		prioritySelector = (Button) findViewById(R.id.priority_selector);
		sendMessage = (Button) findViewById(R.id.btn_send_message);
		messageTextBox = (EditText) findViewById(R.id.message_text_box);
			prepareActionDial();
	}
	
	 /* To set the components listeners. */
	public void addInterfaceListeners(){
		backFromLineOfFireBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	toggleLineOfFireView(v);
		    	dialSelection=dialSelectionSate.UNSELECTED;
		    	updateSideButtonsPositionAndVisibility();
		    }
		});
		backFromSettingsBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	toggleSettingsView(v);
		    	dialSelection=dialSelectionSate.UNSELECTED;
		    	updateSideButtonsPositionAndVisibility();
		    }
		});
		backFromPreDefinedMessageBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	togglePreDefinedMessageView(v);
		    	dialSelection=dialSelectionSate.UNSELECTED;
		    	updateSideButtonsPositionAndVisibility();
		    }
		});
		
		actionDial.setOnTouchListener(this);
		
		backToMainBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					toggleLineOfFireView(v);
					break;
				case MESSAGE:
					toggleMessageView(v);
					hideKeyboard();
					break;
				case SETTINGS:
					toggleSettingsView(v);
					break;
				case PRE_DEFINED_MESSAGES:
					togglePreDefinedMessageView(v);
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
		    	dialSelection=dialSelectionSate.UNSELECTED;
		    	updateSideButtonsPositionAndVisibility();
		    }
		});
		backToLineOfFireBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					// not supposed to happen
					break;
				case MESSAGE:
					toggleMessageView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					hideKeyboard();
					break;
				case SETTINGS:
					toggleSettingsView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					break;
				case PRE_DEFINED_MESSAGES:
					togglePreDefinedMessageView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
		    	toggleLineOfFireView(v);
				dialSelection=dialSelectionSate.LINE_OF_FIRE;
		    	updateSideButtonsPositionAndVisibility();
		    }
		});
		backToSettingsBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					toggleLineOfFireView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					break;
				case MESSAGE:
					toggleMessageView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					hideKeyboard();
					break;
				case SETTINGS:
					// not supposed to happen
					break;
				case PRE_DEFINED_MESSAGES:
					togglePreDefinedMessageView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
		    	toggleSettingsView(v);
				dialSelection=dialSelectionSate.SETTINGS;
		    	updateSideButtonsPositionAndVisibility();
		    }
		});
		backToPreDefinedMessageBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					toggleLineOfFireView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					break;
				case MESSAGE:
					toggleMessageView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					hideKeyboard();
					break;
				case SETTINGS:
					toggleSettingsView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					break;
				case PRE_DEFINED_MESSAGES:
					// not supposed to happen
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
		    	togglePreDefinedMessageView(v);
				dialSelection=dialSelectionSate.PRE_DEFINED_MESSAGES;
		    	updateSideButtonsPositionAndVisibility();
		    }
		});
		backToMessageBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					toggleLineOfFireView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					break;
				case MESSAGE:
					// not supposed to happen
					break;
				case SETTINGS:
					toggleSettingsView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					break;
				case PRE_DEFINED_MESSAGES:
					togglePreDefinedMessageView(v);
					dialSelection=dialSelectionSate.UNSELECTED;
			    	updateSideButtonsPositionAndVisibility();
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
		    	toggleMessageView(v);
				dialSelection=dialSelectionSate.MESSAGE;
		    	updateSideButtonsPositionAndVisibility();
		    }
		});
		
		prioritySelector.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	switch(currentPriorityLevel){
		    		case NORMAL:
		    			prioritySelector.setBackgroundResource(R.drawable.selector_priority_normal_plus);
				    	currentPriorityLevel=PriorityLevel.NORMAL_PLUS;
		    			break;
		    		case NORMAL_PLUS:
		    			prioritySelector.setBackgroundResource(R.drawable.selector_priority_important);
				    	currentPriorityLevel=PriorityLevel.IMPORTANT;
		    			break;
		    		case IMPORTANT:
		    			prioritySelector.setBackgroundResource(R.drawable.selector_priority_critical);
				    	currentPriorityLevel=PriorityLevel.CRITICAL;
		    			break;
					case CRITICAL:
						prioritySelector.setBackgroundResource(R.drawable.selector_priority_normal);
				    	currentPriorityLevel=PriorityLevel.NORMAL;
						break;
					default:
						// inexistent case 
						break;
		    	}
		    }
		});
		sendMessage.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    if(messageTextBox.getText().toString().trim().length()>0){
			    	String msg = messageTextBox.getText().toString().trim();
				    messageTextBox.setText("");
				    sendMessage(msg);
			    }
		    }
		});
}
	
	 /* To toggle between views */
	public void toggleLineOfFireView(View v){
		this.root.toggleLineOfFireView();
	}
	
	public void toggleMessageView(View v){
		this.root.toggleMessagesView();
	}
	
	public void toggleSettingsView(View v){
		this.root.toggleSettingsView();
	}
	public void togglePreDefinedMessageView(View v){
		this.root.togglePreDefinedMessagesView();
	}

	/* to send messages in MessageView */
	public void sendMessage(String msg){
		switch(currentPriorityLevel){
			case NORMAL:
				this.root.postMessage("You", msg, FlyOutContainer.MessageItemPriority.NORMAL, true);
				break;
			case NORMAL_PLUS:
				this.root.postMessage("You", msg, FlyOutContainer.MessageItemPriority.NORMAL_PLUS, true);
				break;
			case IMPORTANT:
				this.root.postMessage("You", msg, FlyOutContainer.MessageItemPriority.IMPORTANT, true);
				break;
			case CRITICAL:
				this.root.postMessage("You", msg, FlyOutContainer.MessageItemPriority.CRITICAL, true);
				break;
			default:
				break;
		}
	}
	
	private void hideKeyboard() {
	    InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

	    // check if no view has focus:
	    View view = this.getCurrentFocus();
	    if (view != null) {
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:
				actioDialCurrentState=actioDialStateEnum.START;
				x=event.getRawX();
				y=event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				if(actioDialCurrentState==actioDialStateEnum.MOVING){
					x=event.getRawX();// - ( actionDial.getWidth() / 2 );
					y=event.getRawY();// - ( actionDial.getHeight() * 5/4 );
				}
				break;
			case MotionEvent.ACTION_UP:
				actioDialCurrentState=actioDialStateEnum.RETURNING;
				setAnimationStartConditions();
				actionDialClean();
				dialState=dialDisplayState.CLOSING;
				switch (dialSelection){
					case LINE_OF_FIRE:
						this.root.toggleLineOfFireView();
						break;
					case MESSAGE:
						this.root.toggleMessagesView();
						break;
					case SETTINGS:
						this.root.toggleSettingsView();
						break;
					case PRE_DEFINED_MESSAGES:
						this.root.togglePreDefinedMessagesView();
						break;
					case UNSELECTED:
						
						break;
					default:
						break;
				}
				updateSideButtonsPositionAndVisibility();
				break;
		}
		dialAnimationHandler.postDelayed(dialAnimationRunnable, dialAnimationPollingInterval);
		return true;
	}

	 /* to control animation timing.. */
	public long actionDialStartTime=0;
	public float actionDialStartX, actionDialStartY=0;
	public long iconsStartTime=0;
	public enum movementInterpolation { LINEAR, OVERSHOOT, EXPONENTIAL};
	public movementInterpolation dialInterpolation = movementInterpolation.OVERSHOOT;
	public void setAnimationactionDialStartTime(){
		actionDialStartTime=Calendar.getInstance().getTimeInMillis();
	}
	public void setAnimationactionIconsStartTime(){
		iconsStartTime=Calendar.getInstance().getTimeInMillis();
	}
	public void setAnimationStartPositions(){
		actionDialStartX=x - actionDialWidth/2.0f;
		actionDialStartY=y - actionDialHeight * 3/2f;
	}
	public void setAnimationStartConditions(){
		setAnimationactionDialStartTime();
		setAnimationStartPositions();
	}
	public float[] getInterpolatedPos(movementInterpolation mode, float actionDialStartX, float actionDialStartY, float endX, float endY, long currentTimeElapsedMilis, long durationMilis, float linearFraction, float frequency, float decay){
		float pos[]={0,0};
		
		if (mode==movementInterpolation.LINEAR){
			float t=(float)currentTimeElapsedMilis/1000.0f;
			float vX = (endX-actionDialStartX)/((durationMilis)/1000.0f);
			float vY = (endY-actionDialStartY)/((durationMilis)/1000.0f);
			
			if (currentTimeElapsedMilis<(durationMilis)){
				pos[0]=actionDialStartX+vX*t;
				pos[1]=actionDialStartY+vY*t;	
			} else {
				pos[0]=actionDialBaseX;
				pos[1]=actionDialBaseY;	
			}
		} else if (mode==movementInterpolation.OVERSHOOT){
			float t=(float)currentTimeElapsedMilis/1000.0f;
			float w=frequency*(float)Math.PI*2;
			float vX = (endX-actionDialStartX)/((durationMilis*linearFraction)/1000.0f);
			float vY = (endY-actionDialStartY)/((durationMilis*linearFraction)/1000.0f);
			
			if(currentTimeElapsedMilis<(durationMilis*linearFraction)){
				pos[0]=actionDialStartX+vX*t;
				pos[1]=actionDialStartY+vY*t;	
			} else if ( (currentTimeElapsedMilis>=(durationMilis*linearFraction)) && (currentTimeElapsedMilis<durationMilis) ){
				pos[0]=endX + vX*((float)Math.sin(t*w)/(float)Math.exp(decay*t)/w);
				pos[1]=endY + vY*((float)Math.sin(t*w)/(float)Math.exp(decay*t)/w);
			} else {
				pos[0]=actionDialBaseX;
				pos[1]=actionDialBaseY;	
			}
		} else if (mode==movementInterpolation.EXPONENTIAL){
			float tau=durationMilis*0.2f;
			if (currentTimeElapsedMilis<(durationMilis)){
				pos[0]=endX + (actionDialStartX - endX)*(float)Math.exp(-currentTimeElapsedMilis/tau);
				pos[1]=endY + (actionDialStartY - endY)*(float)Math.exp(-currentTimeElapsedMilis/tau);
			} else {
				pos[0]=actionDialBaseX;
				pos[1]=actionDialBaseY;	
			}
		} else {
			pos[0]=actionDialBaseX;
			pos[1]=actionDialBaseY;	
		}
		return pos;
	}
	public float[] trimRadialMovement(float xx, float yy, float x0, float y0, float maxRadius){
		float pos[]={0, 0};
		
		float dx = xx-x0;
		float dy = yy-y0;
		
		float radius=(float)Math.sqrt( (Math.pow((double)dx, 2)) + (Math.pow((double)dy, 2)) );
		
		if (radius<=maxRadius){
			pos[0]=xx;
			pos[1]=yy;
		} else {
			double angle = Math.atan2((double)dy, (double)dx);
			
			pos[0]=x0+maxRadius*(float)Math.cos(angle);
			pos[1]=y0+maxRadius*(float)Math.sin(angle);
		}
		
		return pos;
	}
	
	public boolean assertSnapDistance(float xFrom, float yFrom, float xTo, float yTo, float snapRadius){
		float pos[]={0, 0};
		
		float dx = xTo-xFrom;
		float dy = yTo-yFrom;
		
		float radius=(float)Math.sqrt( (Math.pow((double)dx, 2)) + (Math.pow((double)dy, 2)) );
		
		if (radius<=snapRadius){
			return true;
		} else {
			return false;
		}		
	}
	
	 /* To control the animation of the handler */
	protected class DialAnimationRunnable implements Runnable {
		@Override
		public void run() {
			if( dialState==dialDisplayState.OFF ){
				
			} else if( dialState==dialDisplayState.OPENING ) {
				
			} else if( dialState==dialDisplayState.ON ) {
				
			} else if( dialState==dialDisplayState.CLOSING ) {
				/* action dial */
				actionDial.setImageResource(R.drawable.dial_off);
				
				/* action dial circle */
				actionDialCircle.setVisibility(View.INVISIBLE);
				
				/* selection icons */
				lineOfFireIcon.setVisibility(View.INVISIBLE);
				messagesIcon.setVisibility(View.INVISIBLE);
				settingsIcon.setVisibility(View.INVISIBLE);
				preDefMessagesIcon.setVisibility(View.INVISIBLE);
			} else {
				
			}
			
			if ( actioDialCurrentState==actioDialStateEnum.START ){
				/* first set all to transparent */
				lineOfFireIcon.setAlpha(0.0f);
				messagesIcon.setAlpha(0.0f);
				settingsIcon.setAlpha(0.0f);
				preDefMessagesIcon.setAlpha(0.0f);
				actionDialCircle.setScaleX(0.0f);
				actionDialCircle.setScaleY(0.0f);
				actionDialCircle.setAlpha(0.0f);
				
				/* action dial */
				actionDial.setImageResource(R.drawable.dial_on);
				
				/* then schedule the animation */
				lineOfFireIcon.animate().alpha(1.0f).setDuration(dialAnimationInDuration);
				messagesIcon.animate().alpha(1.0f).setDuration(dialAnimationInDuration);
				settingsIcon.animate().alpha(1.0f).setDuration(dialAnimationInDuration);
				preDefMessagesIcon.animate().alpha(1.0f).setDuration(dialAnimationInDuration);
				actionDialCircle.animate().scaleX(1.0f).setDuration(dialAnimationInDuration);
				actionDialCircle.animate().scaleY(1.0f).setDuration(dialAnimationInDuration);
				actionDialCircle.animate().alpha(1.0f).setDuration(dialAnimationInDuration);
				
				/* action dial circle position and visibility */
				actionDialCircle.setX(actionDialCircleBaseX);
				actionDialCircle.setY(actionDialCircleBaseY);
				actionDialCircle.setVisibility(View.VISIBLE);
				
				/* icons position and visibility */
				lineOfFireIcon.setX(lineOfFireIconBaseX);
				lineOfFireIcon.setY(lineOfFireIconBaseY);
				messagesIcon.setX(messagesIconBaseX);
				messagesIcon.setY(messagesIconBaseY);
				settingsIcon.setX(settingsIconBaseX);
				settingsIcon.setY(settingsIconBaseY);
				preDefMessagesIcon.setX(preDefMessagesIconBaseX);
				preDefMessagesIcon.setY(preDefMessagesIconBaseY);
				lineOfFireIcon.setVisibility(View.VISIBLE);
				messagesIcon.setVisibility(View.VISIBLE);
				settingsIcon.setVisibility(View.VISIBLE);
				preDefMessagesIcon.setVisibility(View.VISIBLE);
				
				actioDialCurrentState=actioDialStateEnum.MOVING;
				dialState=dialDisplayState.OPENING;
			} else if ( actioDialCurrentState==actioDialStateEnum.MOVING ){
				float pos[]=trimRadialMovement((x - actionDialWidth/2.0f), (y - actionDialHeight * 3/2f), actionDialBaseX, actionDialBaseY, (float)dialMovementMaxRadius);
				if(assertSnapDistance( pos[0], pos[1], lineOfFireIconBaseX, lineOfFireIconBaseY, iconSnapRadius)){
					pos[0]=lineOfFireIconBaseX - ( (actionDialWidth-lineOfFireIconWidth) / 2 );
					pos[1]=lineOfFireIconBaseY - ( (actionDialHeight-lineOfFireIconHeight) / 2 );
					dialSelection=dialSelectionSate.LINE_OF_FIRE;
					actionDialPostText("Report Line of Fire", PriorityLevel.NORMAL);
					actionDial.setImageResource(R.drawable.dial_selected);
				} else if(assertSnapDistance( pos[0], pos[1], messagesIconBaseX, messagesIconBaseY, iconSnapRadius)){
					pos[0]=messagesIconBaseX - ( (actionDialWidth-messagesIconWidth) / 2 );
					pos[1]=messagesIconBaseY - ( (actionDialHeight-messagesIconHeight) / 2 );
					dialSelection=dialSelectionSate.MESSAGE;
					actionDialPostText("Send Message", PriorityLevel.NORMAL);
					actionDial.setImageResource(R.drawable.dial_selected);
				} else if(assertSnapDistance( pos[0], pos[1], settingsIconBaseX, settingsIconBaseY, iconSnapRadius)){
					pos[0]=settingsIconBaseX - ( (actionDialWidth-settingsIconWidth) / 2 );
					pos[1]=settingsIconBaseY - ( (actionDialHeight-settingsIconHeight) / 2 );
					dialSelection=dialSelectionSate.SETTINGS;
					actionDialPostText("Change Settings", PriorityLevel.NORMAL);
					actionDial.setImageResource(R.drawable.dial_selected);
				} else if(assertSnapDistance( pos[0], pos[1], preDefMessagesIconBaseX, preDefMessagesIconBaseY, iconSnapRadius)){
					pos[0]=preDefMessagesIconBaseX - ( (actionDialWidth-preDefMessagesIconWidth) / 2 );
					pos[1]=preDefMessagesIconBaseY - ( (actionDialHeight-preDefMessagesIconHeight) / 2 );
					dialSelection=dialSelectionSate.PRE_DEFINED_MESSAGES;
					actionDialPostText("Pre Defined Messages", PriorityLevel.NORMAL);
					actionDial.setImageResource(R.drawable.dial_selected);
				} else {
					dialSelection=dialSelectionSate.UNSELECTED;
					actionDialPostText("", PriorityLevel.NORMAL);
					actionDial.setImageResource(R.drawable.dial_on);
				}
				actionDial.setX( pos[0] );
				actionDial.setY( pos[1] );
			} else if ( actioDialCurrentState==actioDialStateEnum.RETURNING ){
				float pos[]=getInterpolatedPos( dialInterpolation, actionDialStartX, actionDialStartY, actionDialBaseX, actionDialBaseY, Calendar.getInstance().getTimeInMillis()-actionDialStartTime, (long)dialReturningAnimationDuration, 0.2f, 5.0f, 20.0f );
				actionDial.setX( pos[0] );
				actionDial.setY( pos[1] );
				if( (pos[0]==actionDialBaseX) && (pos[1]==actionDialBaseY) ){
					actioDialCurrentState=actioDialStateEnum.STILL;
				} else {
					dialAnimationHandler.postDelayed(this, dialAnimationPollingInterval);
				}
			} else if ( actioDialCurrentState==actioDialStateEnum.STILL ){
				actionDial.setX( actionDialBaseX );
				actionDial.setY( actionDialBaseY );
			} else {
				//do nothing
			}
			
		}

	}
	
	/* action dial Text captions */
	public void actionDialPostText(String text, PriorityLevel priority){
		dialText.setText(text);
		dialText.setVisibility(View.VISIBLE);
		dialText.setX(dialTextBaseX);
		dialText.setY(dialTextBaseY);
		
		if(priority==PriorityLevel.NORMAL){
			dialText.setTextSize(20);
		} else if(priority==PriorityLevel.NORMAL_PLUS) {
			dialText.setTextSize(24);
		} else if(priority==PriorityLevel.IMPORTANT) {
			dialText.setTextSize(30);
		} else if(priority==PriorityLevel.CRITICAL) {
			dialText.setTextSize(40);
		}
	}
	
	/* action dial Text captions */
	public void actionDialClean(){
		dialText.setText("");
		dialText.setVisibility(View.INVISIBLE);
	}
}
