package assets.skins
{
import mx.core.DPIClassification;

import spark.skins.mobile.CalloutSkin;

public class BoxyCalloutSkin extends CalloutSkin
{
    public function BoxyCalloutSkin()
    {
        super();

        // this skin does not implement contentBackgroundAppearance=inset
        contentBackgroundInsetClass = null;

        // 是否显示阴影
        dropShadowVisible = true;

        // disable backgroundColor gradient fill
        useBackgroundGradient = false;

        // square up the corners
        contentCornerRadius = 0;
        backgroundCornerRadius = 0;

        // backgroundColor frame is not visible
        frameThickness = 0;

        // 是否显示边框
        borderThickness = 0;
        borderColor = 0x333333;

        // new arrow proportions
        switch (applicationDPI)
        {
            case DPIClassification.DPI_320:
            {
                arrowWidth = 60;
                arrowHeight = 30;
                break;
            }
            case DPIClassification.DPI_240:
            {
                arrowWidth = 45;
                arrowHeight = 15;
                break;
            }
            default:
            {
                arrowWidth = 30;
                arrowHeight = 10;
                break;
            }
        }
    }

    override protected function createChildren():void
    {
        // BoxyCalloutArrow subclasses CalloutArrow

        // create arrow first, super will skip default arrow creation
        arrow = new BoxyCalloutArrow();
        arrow.id = "arrow";
        arrow.styleName = this;

        // call super
        super.createChildren();

        // add arrow above all other children
        addChild(arrow);
    }
}
}