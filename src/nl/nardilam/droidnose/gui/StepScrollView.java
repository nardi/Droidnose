package nl.nardilam.droidnose.gui;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public abstract class StepScrollView extends HorizontalScrollView
{
	private static final double SCROLL_TOLERANCE = 0.1;
	
	private final StepScrollView stepScrollView = this;
	
	private int stepSize;
	private int currentStep;
	
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
					{
						stepScrollView.currentStep += stepDiff;
						stepScrollView.onStepChange(stepScrollView.currentStep);
					}

					stepScrollView.smoothScrollTo(stepScrollView.currentStep * stepSize, 0);
					
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
	
	public void scrollToStep(int step)
	{
		this.scrollTo(step * this.stepSize, 0);
		this.currentStep = step;
	}
	
	protected abstract void onStepChange(int step);
}
