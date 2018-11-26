# EmpsunSppUtils
### 使用集成:

```
allprojects {
    repositories {
        google()
        jcenter()
        //在Project的grade文件中添加
        maven { url 'https://jitpack.io' }
    }
}
```
```
//在Module添加依赖
implementation 'com.github.CHENDUEMPSUN:EmpsunSppUtils:v1.0.0'
```

### 获得SppUtils对象

```
 SppUtils sppUtils = new SppUtils(this);
```
