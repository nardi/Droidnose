package nl.nardilam.droidnose.gui;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public abstract class StepScrollView extends HorizontalScrollView
{
	private static final double SCROLL_TOLERANCE = 0.11;
	
	private final StepScrollView stepScrollView = this;
	
	protected int stepSize;
	protected int currentStep;
	
	public StepScrollView(Context context)
	{
		super(context);
		
		this.setOnTouchListener(new OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				int action = event.getAction();
				if ((action == MotionEvent.ACTION_UP
				 || action == MotionEvent.ACTION_CANCEL)
				 && stepScrollView.stepSize != 0)
				{
					double stepPosition = (double)stepScrollView.getScrollX() / stepScrollView.stepSize;
					double distanceFromCurrentStep = stepPosition - stepScrollView.currentStep;
					int stepDiff = (int)(Math.signum(distanceFromCurrentStep) *
							Math.ceil(Math.abs(distanceFromCurrentStep)));
					
					if (Math.abs(distanceFromCurrentStep) > SCROLL_TOLERANCE)
						stepScrollView.currentStep += stepDiff;
					
					stepScrollView.onStepChange(stepScrollView.currentStep);
					
					return true;
				}
				return false;
			}
		});
	}

	public void setStepSize(int s)
	{
		this.stepSize = s;
	}
	
	public int getCurrentStep()
	{
		return this.currentStep;
	}
	
	public void goToStep(int step)
	{
		this.currentStep = step;
		this.post(new Runnable()
		{
			public void run()
			{
				stepScrollView.smoothScrollTo(currentStep * stepSize, 0);
			}
		});
	}
	
	protected abstract void onStepChange(int step);
}
