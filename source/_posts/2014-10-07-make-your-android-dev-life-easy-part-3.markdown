---
layout: post
title: "让你的安卓开发更容易(三)——Picasso"
date: 2014-10-07 23:48
description: P让你的安卓开发更容易
keywords: andorid,picasso
comments: true
categories: code
tags: [android,picasso]
---
 
{% img /images/post/2014-10/picasso.png %}  
  
[Picasso][picasso]是Android下一个强大的图片下载和缓存类库，代码借口简洁易懂，功能强大，Picasso有如下特性：  
  
* 处理Adapter中的ImageView回收和取消已经回收ImageView的下载进程
* 使用最少的内存完成复杂的图片转换，比如把下载的图片转换为圆角等
* 自动添加磁盘和内存缓存
  
下面通过介绍Android原生的图片下载缓存功能和Picasso进行对比，看看使用Picasso有哪些好的地方。  
  
<!--more-->
## Android原生的图片下载功能
在没有使用Picasso的情况下，如果想做到图片下载以及缓存，需要编写大量代码。  
  
#### 图片下载
Android的图片下载是不能在主线程里面进行的，需要新创建一个线程进行操作。  

* 首先要继承AsyncTask类，Android的一个异步操作类。  

{% codeblock DownloadTask.java lang:java %}

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import com.github.zzm.bushu.app.model.LogTag;

import java.io.*;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Void, String> {
	private File imageFile;
    private ImageView imageView;

    public DownloadTask(File imageFile, ImageView imageView) {
        this.imageFile = imageFile;
        this.imageView = imageView;
    }
	...
}
{% endcodeblock %}   
  
* 实现`doInBackground`方法。  
  
该方法接受一个可变String参数，表示可以进行多个url下载，但这url参数是从哪里传进来的呢？  
  
我们后面在使用这个`DownloadTask`类时，会调用其`exexute(String... url)`的方法，url参数就是从这里传进去的。  
  
下面的代码中通过`new URL(url).openStream()`进行图片下载，然后新建一个文件输出流，将图片写到输出文件中。  
  
{% codeblock DownloadTask.java lang:java %}
    @Override
    protected String doInBackground(String... urls) {
        storageImage(urls[0]);
        return null;
    }

    private void storageImage(String url) {
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(imageFile);
            outputStream.write(getImageBytes(url));
            outputStream.close();
        } catch (Exception e) {
            Log.e(LogTag.DownloadTask.name(), "storage image error:" + e.getMessage());
        }
    }

    private byte[] getImageBytes(String url) throws IOException {
        InputStream in = new BufferedInputStream(new URL(url).openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while (-1 != (n = in.read(buf))) {
            out.write(buf, 0, n);
        }
        out.close();
        in.close();
        return out.toByteArray();
    }
}
{% endcodeblock %}   
    
* 实现`onPostExecute`方法，这个方法是在图片下载完成后调用的，我们可以将下载的图片指定显示到某个imageView中。  
  
{% codeblock DownloadTask.java lang:java %}
    @Override
    protected void onPostExecute(String ignore) {
        imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
    }
{% endcodeblock %}   
  
#### 图片缓存
我们要自己实现图片缓存的功能也比较简单，代码如下。(但如果已经有Picasso这种强大的类库，我们又何必自己造轮子呢？)  
  
* 要找到下载的图片文件，我们要自己定义文件的命名规则和存放路径，这样才能方便我们找到文件。  
  
{% codeblock MyAdapter.java lang:java %}
    File imageFile = getImageFile(bookName);

    private File getImageFile(String bookName) {
        File imageFile = new File(context.getFilesDir(), bookName + ".png");
        Log.d(LogTag.BooksAdapter.name(), "file path: " + imageFile.getAbsolutePath());
        return imageFile;
    }
{% endcodeblock %}   
  
* 判断文件是否存在，如果不存在则进行首次下载，如果已经存在了则直接从磁盘上面加载文件。  
  
{% codeblock MyAdapter.java lang:java %}
    if (imageFileEmpty(imageFile)) {
            downloadImage(bookName, imageView);
        } else {
            imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
        }
{% endcodeblock %}   
  
* 判断是否有网络，有的话进行图片下载，调用刚才介绍的`DownloadTask`类，然后调用`execute`的方法即可，这样后台就会异步帮你将图片下载下来，然后进行显示。  
  
{% codeblock MyAdapter.java lang:java %}
    private void downloadImage(String bookName, ImageView imageView) {
        if (networkOk()) {
            String url = format("%s%s/%s.png", STORAGE_BASE_URL, getScreenDensity(), bookName);
            Log.d(LogTag.BooksAdapter.name(), format("url: %s", url));
            new DownloadTask(getImageFile(bookName), imageView).execute(url);
        }
    }

    private boolean networkOk() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
{% endcodeblock %}   
  
## Picasso的图片下载功能
看完Android的图片下载功能，发现我们写了不少代码，现在来看看Picasso是怎么实现的。  
  
{% codeblock MyAdapter.java lang:java %}
	Picasso.with(getContext()).load(url).into((ImageView) viewImage);
{% endcodeblock %}   
  
一句话就搞定了，就是这么简单，是不是觉得生活美好了很多。  

Picasso会在应用的cache目录下新建一个`picasso-cache`文件夹，里面就是picasso的图片缓存文件。  
 
{% img /images/post/2014-10/picasso-cache.png %}  
  
## 相关链接

* [让你的安卓开发更容易(二)——Genymotion][part2]  
* [让你的安卓开发更容易(一)——Intellij IDEA][part1]  


[picasso]: http://square.github.io/picasso/
[part2]: http://zhaozhiming.github.io/blog/2014/08/31/make-your-android-dev-life-easy-part-2/
[part1]: http://zhaozhiming.github.io/blog/2014/08/31/make-your-android-dev-life-easy-part-1/