package setec.g3.ui.view.viewgroup;

import java.util.ArrayList;

import setec.g3.ui.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class FlyOutContainer extends RelativeLayout {

	/* References to groups contained in this view. */
	private View lineOfFireView;
	private View messageView;
		private ListView messageList;
		private ArrayList<MessageItem> messageItemValues;
		private MessageItemArrayAdapter messageAdapter;
	private View settingsView;
	private View preDefinedMessageView;
	private View primaryView;
	
	/* messaging */
	static public enum MessageItemPriority{NORMAL, NORMAL_PLUS, IMPORTANT, CRITICAL};

	/* Constants */
	protected static final int secondaryMargin = 150;

	public enum secondaryViewState { CLOSED, OPEN, CLOSING, OPENING	};

	/* Position information attributes */
	protected int currentPrimaryHorizontalOffset = 0;
	protected int currentPrimaryVerticalOffset = 0;
	protected secondaryViewState lineOfFireCurrentState = secondaryViewState.CLOSED;
	protected secondaryViewState messageCurrentState = secondaryViewState.CLOSED;
	protected secondaryViewState settingsCurrentState = secondaryViewState.CLOSED;
	protected secondaryViewState preDefMessageCurrentState = secondaryViewState.CLOSED;

	/* animation objects */
    protected Scroller secondaryViewHorizontalAnimationScroller = new Scroller(this.getContext(), new SmoothInterpolator());
    protected Scroller secondaryViewVerticalAnimationScroller = new Scroller(this.getContext(), new SmoothInterpolator()); 
	protected Runnable secondaryViewAnimationRunnable = new AnimationRunnable();
	protected Handler secondaryViewAnimationHandler = new Handler();
	
	/* Animation constants */
	private static final int secondaryViewAnimationDuration = 500;
	private static final int secondaryViewAnimationPollingInterval = 16;
	
	public FlyOutContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FlyOutContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FlyOutContainer(Context context) {
		super(context);
	}

	public int getSideMargin(){
		return secondaryMargin;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		this.messageView = this.getChildAt(0);
		this.settingsView = this.getChildAt(1);
		this.preDefinedMessageView = this.getChildAt(2);
		this.lineOfFireView = this.getChildAt(3);
		
		this.primaryView = this.getChildAt(4);
		this.primaryView.setBackgroundResource(R.drawable.background);
		
		this.messageView.setVisibility(View.GONE);
		this.settingsView.setVisibility(View.GONE);
		this.preDefinedMessageView.setVisibility(View.GONE);
		this.lineOfFireView.setVisibility(View.GONE);
		
		/* message listview & messaging */
		messageList = (ListView) findViewById(R.id.listview);
		messageItemValues = new ArrayList<MessageItem>();
		messageAdapter = new MessageItemArrayAdapter((Activity) this.getContext(), messageItemValues);
		messageList.setAdapter(messageAdapter);
		
		
		postMessage("You", "Hi guys...", MessageItemPriority.NORMAL, true);
		postMessage("Command", "What do you want?", MessageItemPriority.NORMAL_PLUS, false);
		postMessage("You", "I just wanna talk...", MessageItemPriority.IMPORTANT, true);
		postMessage("Command", "Dude, shut the hell up!", MessageItemPriority.CRITICAL, false);
		postMessage("You", "But why?", MessageItemPriority.NORMAL, true);
		postMessage("Command", "Man, you are a pain in the ass!", MessageItemPriority.NORMAL, false);
		postMessage("You", "You are being mean :(", MessageItemPriority.NORMAL, true);
		postMessage("Command", "LOL, don't be a little girl..", MessageItemPriority.NORMAL, false);
		postMessage("You", "Stop it!", MessageItemPriority.NORMAL, true);
		postMessage("Command", "Will", MessageItemPriority.NORMAL, false);
		postMessage("Command", "you", MessageItemPriority.NORMAL, false);
		postMessage("Command", "man", MessageItemPriority.NORMAL, false);
		postMessage("Command", "UP!!", MessageItemPriority.CRITICAL, false);
		postMessage("You", "You know what?", MessageItemPriority.NORMAL, true);
		postMessage("You", "You put out these flames!", MessageItemPriority.CRITICAL, true);
		postMessage("You", "I QUIT!", MessageItemPriority.CRITICAL, true);
		postMessage("Command", "NOOOO!!", MessageItemPriority.CRITICAL, false);
		postMessage("Command", "Please don't go!", MessageItemPriority.CRITICAL, false);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed){
			this.calculateChildDimensions();
		}
		
		this.lineOfFireView.layout(left /*+ this.currentPrimaryHorizontalOffset*/, top /*+ this.currentPrimaryVerticalOffset*/, right /*+ this.currentPrimaryHorizontalOffset*/, bottom -secondaryMargin /*+ this.currentPrimaryVerticalOffset*/);
		this.messageView.layout(left + secondaryMargin /*+ this.currentPrimaryHorizontalOffset*/, top /*+ this.currentPrimaryVerticalOffset*/, right /*+ this.currentPrimaryHorizontalOffset*/, bottom /*+ this.currentPrimaryVerticalOffset*/);
		this.settingsView.layout(left /*+ this.currentPrimaryHorizontalOffset*/, top + secondaryMargin  /*+ this.currentPrimaryVerticalOffset*/, right /*+ this.currentPrimaryHorizontalOffset*/, bottom /*+ this.currentPrimaryVerticalOffset*/);
		this.preDefinedMessageView.layout(left /*+ this.currentPrimaryHorizontalOffset*/, top /*+ this.currentPrimaryVerticalOffset*/, right - secondaryMargin /*+ this.currentPrimaryHorizontalOffset*/, bottom /*+ this.currentPrimaryVerticalOffset*/);
		this.primaryView.layout(left + this.currentPrimaryHorizontalOffset, top + this.currentPrimaryVerticalOffset, right + this.currentPrimaryHorizontalOffset, bottom + this.currentPrimaryVerticalOffset);
	}

	public void toggleLineOfFireView() {
		switch (this.lineOfFireCurrentState) {
		case CLOSED:
			this.lineOfFireCurrentState = secondaryViewState.OPENING;
			this.lineOfFireView.setVisibility(View.VISIBLE);
			this.secondaryViewVerticalAnimationScroller.startScroll(0, 0, 0, this.lineOfFireView.getLayoutParams().height, secondaryViewAnimationDuration);	
			break;
		case OPEN:
			this.lineOfFireCurrentState = secondaryViewState.CLOSING;
			this.secondaryViewVerticalAnimationScroller.startScroll(0, this.currentPrimaryVerticalOffset, 0, -this.currentPrimaryVerticalOffset, secondaryViewAnimationDuration);
			break;
		default:
			return;
		}

		this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, secondaryViewAnimationPollingInterval);
		
		this.invalidate();
	}
	
	public void toggleMessagesView() {
		switch (this.messageCurrentState) {
		case CLOSED:
			this.messageCurrentState = secondaryViewState.OPENING;
			this.messageView.setVisibility(View.VISIBLE);
			this.secondaryViewHorizontalAnimationScroller.startScroll(0, 0, -this.messageView.getLayoutParams().width, 0, secondaryViewAnimationDuration);	
			break;
		case OPEN:
			this.messageCurrentState = secondaryViewState.CLOSING;
			this.secondaryViewHorizontalAnimationScroller.startScroll(this.currentPrimaryHorizontalOffset, 0, -this.currentPrimaryHorizontalOffset, 0, secondaryViewAnimationDuration);
			break;
		default:
			return;
		}

		this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, secondaryViewAnimationPollingInterval);
		
		this.invalidate();
	}
	
	public void toggleSettingsView() {
		switch (this.settingsCurrentState) {
		case CLOSED:
			this.settingsCurrentState = secondaryViewState.OPENING;
			this.settingsView.setVisibility(View.VISIBLE);
			this.secondaryViewVerticalAnimationScroller.startScroll(0, 0, 0, -this.settingsView.getLayoutParams().height, secondaryViewAnimationDuration);	
			break;
		case OPEN:
			this.settingsCurrentState = secondaryViewState.CLOSING;
			this.secondaryViewVerticalAnimationScroller.startScroll(0, this.currentPrimaryVerticalOffset, 0, -this.currentPrimaryVerticalOffset, secondaryViewAnimationDuration);
			break;
		default:
			return;
		}

		this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, secondaryViewAnimationPollingInterval);
		
		this.invalidate();
	}
	
	public void togglePreDefinedMessagesView() {
		switch (this.preDefMessageCurrentState) {
		case CLOSED:
			this.preDefMessageCurrentState = secondaryViewState.OPENING;
			this.preDefinedMessageView.setVisibility(View.VISIBLE);
			this.secondaryViewHorizontalAnimationScroller.startScroll(0, 0, this.preDefinedMessageView.getLayoutParams().width, 0, secondaryViewAnimationDuration);	
			break;
		case OPEN:
			this.preDefMessageCurrentState = secondaryViewState.CLOSING;
			this.secondaryViewHorizontalAnimationScroller.startScroll(this.currentPrimaryHorizontalOffset, 0, -this.currentPrimaryHorizontalOffset, 0, secondaryViewAnimationDuration);
			break;
		default:
			return;
		}

		this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, secondaryViewAnimationPollingInterval);
		
		this.invalidate();
	}

	private void calculateChildDimensions() {
		this.primaryView.getLayoutParams().height = this.getHeight();
		this.primaryView.getLayoutParams().width = this.getWidth();

		this.lineOfFireView.getLayoutParams().width = this.getWidth();
		this.lineOfFireView.getLayoutParams().height = this.getHeight() - secondaryMargin;
		
		this.messageView.getLayoutParams().width = this.getWidth() - secondaryMargin;
		this.messageView.getLayoutParams().height = this.getHeight();
		
		this.settingsView.getLayoutParams().width = this.getWidth();
		this.settingsView.getLayoutParams().height = this.getHeight() - secondaryMargin;
		
		this.preDefinedMessageView.getLayoutParams().width = this.getWidth() - secondaryMargin;
		this.preDefinedMessageView.getLayoutParams().height = this.getHeight();
	}
	
	private void adjustContentPosition(boolean isAnimationOngoing) {
		int scrollerHorizontalOffset = this.secondaryViewHorizontalAnimationScroller.getCurrX();
		int scrollerVerticalOffset = this.secondaryViewVerticalAnimationScroller.getCurrY();

		this.primaryView.offsetLeftAndRight(scrollerHorizontalOffset - this.currentPrimaryHorizontalOffset);
		this.primaryView.offsetTopAndBottom(scrollerVerticalOffset - this.currentPrimaryVerticalOffset);

		this.currentPrimaryHorizontalOffset = scrollerHorizontalOffset;
		this.currentPrimaryVerticalOffset = scrollerVerticalOffset;

		this.invalidate();
		
		if (isAnimationOngoing)
			this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, secondaryViewAnimationPollingInterval);
		else
			this.onSecondaryViewTransitionComplete();
	}
	
	private void onSecondaryViewTransitionComplete() {
		switch (this.lineOfFireCurrentState) {
		case OPEN:
			break;
		case CLOSED:
			break;
		case OPENING:
			this.lineOfFireCurrentState = secondaryViewState.OPEN;
			break;
		case CLOSING:
			this.lineOfFireCurrentState = secondaryViewState.CLOSED;
			this.lineOfFireView.setVisibility(View.GONE);
			break;
		default:
			return;
		}
		
		switch (this.messageCurrentState) {
		case OPEN:
			break;
		case CLOSED:
			break;
		case OPENING:
			this.messageCurrentState = secondaryViewState.OPEN;
			//postMessage();
			break;
		case CLOSING:
			this.messageCurrentState = secondaryViewState.CLOSED;
			this.messageView.setVisibility(View.GONE);
			break;
		default:
			return;
		}
		
		switch (this.settingsCurrentState) {
		case OPEN:
			break;
		case CLOSED:
			break;
		case OPENING:
			this.settingsCurrentState = secondaryViewState.OPEN;
			break;
		case CLOSING:
			this.settingsCurrentState = secondaryViewState.CLOSED;
			this.settingsView.setVisibility(View.GONE);
			break;
		default:
			return;
		}
		
		switch (this.preDefMessageCurrentState) {
		case OPEN:
			break;
		case CLOSED:
			break;
		case OPENING:
			this.preDefMessageCurrentState = secondaryViewState.OPEN;
			break;
		case CLOSING:
			this.preDefMessageCurrentState = secondaryViewState.CLOSED;
			this.preDefinedMessageView.setVisibility(View.GONE);
			break;
		default:
			return;
		}
	}
	
	protected class SmoothInterpolator implements Interpolator{
		@Override
		public float getInterpolation(float t) {
			return (float)Math.pow(t-1, 5) + 1;
		}
	}
	
	protected class AnimationRunnable implements Runnable {
		@Override
		public void run() {
			FlyOutContainer.this.adjustContentPosition(FlyOutContainer.this.secondaryViewHorizontalAnimationScroller.computeScrollOffset());
			FlyOutContainer.this.adjustContentPosition(FlyOutContainer.this.secondaryViewVerticalAnimationScroller.computeScrollOffset());
		}
	}	
	
	protected class MessageItem{
		public String sndr, msg;
		public MessageItemPriority priority;
		boolean sentByOwner;
		
		public MessageItem(String sender, String message, MessageItemPriority priorityLevel, boolean sentByMe){
			this.sndr=new String(sender);
			this.msg=new String(message);
			this.priority=priorityLevel;
			this.sentByOwner=sentByMe;
		}
	}
	
	public class MessageItemArrayAdapter extends ArrayAdapter<MessageItem> {
		  private final Activity context;
		  private final ArrayList<MessageItem> values;

		  class ViewHolder {
			    public TextView text;
		  }

		  public MessageItemArrayAdapter(Activity context, ArrayList<MessageItem> values) {
		    super(context, R.layout.row_layout, values);
		    this.context = context;
		    this.values = values;
		  }

		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		    View rowView = convertView;
		    /* reuse views */
		    if (rowView == null) {
		      LayoutInflater inflater = context.getLayoutInflater();
		      rowView = inflater.inflate(R.layout.row_layout, null);
		      
		      /* configure view holder */
		      ViewHolder viewHolder = new ViewHolder();
		      viewHolder.text = (TextView) rowView.findViewById(R.id.message_text_view);
		      rowView.setTag(viewHolder);
		    }

		    /* process test */
		    ViewHolder holder = (ViewHolder) rowView.getTag();
		    SpannableString textToProcess = new SpannableString((new StringBuilder(values.get(position).sndr).append(":\n").append(values.get(position).msg)).toString());
		    textToProcess.setSpan(new ForegroundColorSpan((values.get(position).sentByOwner)?(Color.GRAY):(Color.WHITE)), 0, values.get(position).sndr.length()+1, 0);
		    textToProcess.setSpan(new RelativeSizeSpan(0.7f), 0, values.get(position).sndr.length()+1, 0);
		    
		    /* fill text view */
		    holder.text.setText(textToProcess, BufferType.SPANNABLE);

		    /* send speech balloon to left or right, accordingly to the sender */
		    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		    p.addRule((values.get(position).sentByOwner == true)?(RelativeLayout.ALIGN_PARENT_RIGHT):(RelativeLayout.ALIGN_PARENT_LEFT));
		    if(values.get(position).sentByOwner == true){
		    	p.rightMargin=25;
		    } else {
		    	p.leftMargin=25;
		    }
		    holder.text.setLayoutParams(p);
		      
		    /* see which priority has the message */
		    switch (values.get(position).priority){
		    	case NORMAL:
		    		holder.text.setBackgroundResource(R.drawable.speech_buble_normal);
		    		break;
		    	case NORMAL_PLUS:
		    		holder.text.setBackgroundResource(R.drawable.speech_buble_normal_plus);
		    		break;
		    	case IMPORTANT:
		    		holder.text.setBackgroundResource(R.drawable.speech_buble_important);
		    		break;
		    	case CRITICAL:
		    		holder.text.setBackgroundResource(R.drawable.speech_buble_critical);
		    		break;
	    		default:
	    			holder.text.setBackgroundResource(R.drawable.speech_buble_normal);
	    			break;
		    }
		    
		    return rowView;
		  }
		}
	
		public void postMessage(String sender, String message, MessageItemPriority priority, boolean sentByMe){
			messageItemValues.add(new MessageItem(sender, message, priority, sentByMe));
			messageList.setSelection(messageAdapter.getCount() - 1);
		}
}
