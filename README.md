# WaveView
双波纹圆形进度视图，类似迅雷的桌面图标动态显示进度动画

1. 设置圆形半径
    app:radius="60dp"
    
2. 设置背景图，设置背景图才能透视到地图，也就是说把地图扣出来填充圆形达到透视效果。
    waveView.setBackground(bitmap);
    （因为大部分圆形图像大部分都是通过Paint设置一个PorterDuff.Mode.SRC_IN的PorterDuffXfermode
    来实现，底下绘制一个圆，上层用设置过Xfermode的绘笔draw一个bitmap上去达到圆形图像的目的。
    由于此效果有两条波纹，后面那条是一个透明度的渐变，而设置Xfermode去融合图层是不含透明度，
    也就是看不到透视效果，所以就在底层绘制一个背景图抠出来的一部分贴上去）

3. 设置波纹移动速度，第一个参数是第一条波纹的速度，第二个是第二条波纹的速度
    waveView.setWaveSpeed(5, 9);    

4. 设置进度，注意其中参数是从0到1到进度
    waveView.setPercent(percent);
    

![demo](https://raw.githubusercontent.com/Bvin/WaveView/master/demo/src/main/assets/demo.jpg)