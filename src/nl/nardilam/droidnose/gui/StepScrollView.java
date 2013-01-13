package nl.nardilam.droidnose.gui;

import nl.nardilam.droidnose.Callback;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public abstract class StepScrollView extends HorizontalScrollView
{
	private static final double SCROLL_TOLERANCE = 0.11;
	
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
					final boolean stepChange = Math.abs(distanceFromCurrentStep) > SCROLL_TOLERANCE;
					
					if (stepChange)
						stepScrollView.currentStep += stepDiff;

					stepScrollView.smoothScrollTo(stepScrollView.currentStep * stepSize, new Callback<Integer>()
					{
						public void onResult(Integer result)
						{
							if (stepChange)
								stepScrollView.onStepChange(stepScrollView.currentStep);
						}
						public void onError(Exception e)
						{
							// Too bad
						}
					});
					
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
		this.scrollTo(step * this.stepSize, 0);
		this.currentStep = step;
	}
	
	protected abstract void onStepChange(int step);
	
	int scrollDestination = -1;
	Callback<Integer> scrollCallback = null;
	
	public void smoothScrollTo(int x, Callback<Integer> callback)
	{ 
		this.scrollDestination = x;
		this.scrollCallback = callback;
		this.smoothScrollTo(x, 0);
	}
	
	protected void onScrollChanged(int x, int y, int oldx, int oldy)
	{
		if (this.scrollCallback != null && x == this.scrollDestination)
		{
			this.scrollCallback.onResult(x);
			this.scrollDestination = -1;
		}
	}
}
