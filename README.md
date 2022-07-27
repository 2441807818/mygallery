# 一、作业要求
尝试仿照系统相册实现一个图库APP：  

![origin_img_v2_7e0c7ae0-22af-4236-bc59-29bfe4da538g.jpg](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/070abdd1eaa24907b453e9d1a2dfdc35~tplv-k3u1fbpfcp-watermark.image?)
# 二、作业分析
我们需要先对程序进行需求分析，想一想一个简单的图库App中应该具备哪些功能。将这些功能全部整理出来之后，我们才好一一实现。这里我认为一个图库App至少应该具备以下功能：  
- 可以展示我们手机的相册。
- 可以查看每个相册的图片。
- 可以对每个图片进行大图浏览。  

分析完功能需求之后，我们从几大方面去分析应该怎么去实现。  
1. 我们可以通过ContentProvider(内容提供者)这个组件去获取手机的照片数据。
2. 我们需要至少三个界面（一个用于展示相册、一个用于展示相册内容、一个用于展示大图），我们这里使用Activity+Fragment的页面架构，当然你也可以使用三个Activity。
3. 获取手机图片需要**读取手机存储**的权限，我们需要在清单文件中声明，由于此权限系统认为是**危险权限**，在android6.0及以上版本需要动态申请权限。
# 三、App实现
先给大家看一下我做的效果：  
<img src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2f5542fff3234c468aa08fc263934007~tplv-k3u1fbpfcp-watermark.image?" alt="飞书20220727-182955.gif" width="30%" />  
经过上述的分析，我们的任务就很清晰了，然后就开始实现吧。
## 1. 数据获取
### 获取系统相册
通过ContentResolver可以去查询手机图片。  
我们先看一下ContentResolver的查询方法。
```
public final Cursor query (Uri uri, 
                String[] projection, 
                Bundle queryArgs, 
                CancellationSignal cancellationSignal)
public final Cursor query (Uri uri, 
                String[] projection, 
                String selection, 
                String[] selectionArgs, 
                String sortOrder)
```  
这是方法的关键变量描述：
|  参数名 | 描述 |
| - | - |
| uri | 用于检索内容的 URI，使用 content:// 方案。该值不能为`null` |
| projection | 要返回的列的列表，传递 null 将返回所有列。|
| selection | 条件过滤器，格式化为 SQL WHERE 子句（不包括 WHERE 本身），传递 null 将返回所有行。|
| selectionArgs | 您可以在选择中包含?，它将被 selectionArgs 中的值替换，按照它们在选择中出现的顺序。这些值将绑定为字符串。这个值可能是`null`。 |
| sortOrder | 如何对行进行排序，格式化为 SQL ORDER BY 子句（不包括 ORDER BY 本身）|
| queryArgs | 包含操作所需的附加信息的 Bundle。参数可能包括 SQL 样式参数，例如，但请注意，每个单独提供程序的文档将指示它们支持哪些参数。 |

我们只需要相册，所以需要对数据进行去重，也就是**根据列分组**。    
projection是我们筛选的列，对于相册，我们只需要**相册名称、相册ID、相册封面图**即可。  
BUCKET_DISPLAY_NAME：相册名称  
BUCKET_ID：相册ID  
DATA：图片路径  
`date_added`：相册创建时间  
因为query方法不能直接进行分组操作，这里我们在selection这里用了一个小技巧，对相册名称进行分组，并按相册添加时间进行降序。
```
ContentResolver resolver = context.getContentResolver();
// 目标Uri
Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
String[] projection = new String[]{
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.DATA
};
cursor = resolver.query(uri, projection, "0=0) group by (bucket_display_name", null, "date_added desc");
```
但在android 11及之后，谷歌官方做了一些变动，**为了一些安全（防止SQL注入）**，所以不能之间在query方法上通过显式地进行分组，排序等操作，**需要在Bundle中传递你的更多要求**（比如分组、排序、分页等操作）。  
为了适配更高版本的android系统，我也写了高版本的代码。  
```
projection = new String[]{
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.DATA};
Bundle bundle = new Bundle();
// 给BUCKET_DISPLAY_NAME分组
bundle.putString(ContentResolver.QUERY_ARG_SQL_GROUP_BY,MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
// 设置排序
bundle.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER,"date_added desc");
cursor = resolver.query(uri, projection, bundle, null);
```
这里我定义了一个**Bucket实体类**用于存放相册的消息，当然遍历完数据记得关闭cursor。
```
if (cursor != null) {
    while (cursor.moveToNext()) {
        Bucket bucket = new Bucket();
        bucket.setBucketName(cursor.getString(0));
        bucket.setBucketId(cursor.getInt(1));
        bucket.setCover(cursor.getString(2));
        buckets.add(bucket);
    }
    cursor.close();
}
```
这样我们就完成了获取相册的任务。
### 通过相册获取图片
根据相册获取获取图片和上面的代码也差不多，我们利用上面**返回的相册ID**再通过**query方法筛选**来得到此相册的图片。  
具体实现代码：
```
// 内容接收者
ContentResolver resolver = context.getContentResolver();
// 目标Uri
Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
// 筛选列值
String[] projection = new String[]{
        MediaStore.Images.Media.DATA
};
// 结果游标
Cursor cursor;
if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // 适配android11
    Bundle bundle = new Bundle();
    // 设置查询条件
    bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "bucket_id=?");
    bundle.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, new String[]{String.valueOf(BucketId)});
    cursor = resolver.query(uri, projection, bundle, null);
} else {
    cursor = resolver.query(uri, projection, "bucket_id=?", new String[]{String.valueOf(BucketId)}, null);
}
```
这样我们的数据获取就完成了，接下来就是开始实现界面了。  
## 2. 界面实现
我采用单Activity + Fragment的方式实现这个图库App。  
Activity的布局文件就只有一个布局 -> FragmentContainerView  
然后我创建了三个Fragment（首页、相册内容页、大图页）以及对应的布局文件。  
我在@navigation/activity_main文件中指定了这三个fragment，指定了它们的动作是首页 -> 相册内容页 -> 大图页。  
```
<androidx.fragment.app.FragmentContainerView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/fragment_container_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:name="androidx.navigation.fragment.NavHostFragment"
    app:defaultNavHost="true"
    app:navGraph="@navigation/activity_main"
    tools:context=".view.MainActivity" />
```
记得Activity要在清单文件中声明哦。接下来我们看一下三个Fragment的具体布局。   
### 相册页面(首页)
<img src="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a98f44c349b4480bbc894d9c2c82cba6~tplv-k3u1fbpfcp-watermark.image?" alt="origin_img_v2_2e8bc72f-245f-4e1d-9df3-b543b969f03g.jpg" width="30%" />  

我们根据图片分析，需要一个网格列表去显示我们的相册，我们可以选择**GridLayout + Adapter或者RecyclerView + Adapter**，这里我选用RecyclerView + Adapter的方式。  
1. 在相册页面的布局文件中加入RecyclerView  
**RecyclerView是一个很灵活的列表**，它支持多种布局管理器，而且相比ListView也进行了很多性能优化。
fragment_home.xml(布局文件代码)
```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".view.home.HomeFragment">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</LinearLayout>
```
2. 可以看到图中的每个相册都是一个item，我们还需要创建一个布局文件去实现item的布局代码。 
list_bucket_view.xml(item布局代码)
```
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="150dp"
    android:layout_margin="10dp">

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ImageView
            android:id="@+id/coverView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:src="@mipmap/ic_launcher"
            android:scaleType="centerCrop"/>

        <TextView
            android:id="@+id/bucketName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="相册名称"
            android:layout_alignParentBottom="true"
            android:layout_alignParentEnd="true"
            android:padding="5dp"
            android:layout_margin="10dp"
            android:textColor="@color/white"
            android:maxLength="10"
            android:ellipsize="end"
            android:background="#66000000"/>

    </RelativeLayout>

</androidx.cardview.widget.CardView>
```
3. 在HomeFragment中动态检查“读取手机存储”权限，没有这个权限我们是获取不到图片的，而且还会报错。  
当然首先要在清单文件中声明权限
```
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```
```
@Override
public void onStart() {
    super.onStart();
    // 检查读取手机存储权限
    if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
        // 没有权限，去申请
        ActivityCompat.requestPermissions(requireActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        return;
    }
    ...
}
// 权限申请回调事件
@Override
public void onPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(requireActivity(), "授权成功", Toast.LENGTH_SHORT).show();
        onStart();
    } else {
        Toast.makeText(requireActivity(),"授权失败，软件无法正常使用",Toast.LENGTH_SHORT).show();
        requireActivity().finish();
    }
}
```
4. 将数据显示到列表中，我们需要创建一个Adapter,对于RecyclerView我们还需要创建一个Holder。   
这里还定义了一个OnToPhotoListener接口，用于设置相册点击事件。
```
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.BucketHolder> {

    private final List<Bucket> buckets;

    private final OnToPhotoListener onToPhotoListener;

    public HomeAdapter(List<Bucket> buckets, OnToPhotoListener onToPhotoListener) {
        this.buckets = buckets;
        this.onToPhotoListener = onToPhotoListener;
    }

    @NonNull
    @Override
    public BucketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_bucket_view, parent,false);
        return new BucketHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull BucketHolder holder, int position) {
        Bucket bucket = buckets.get(position);
        holder.bucketName.setText(bucket.getBucketName());
        Glide.with(holder.coverView.getContext()).load(new File(bucket.getCover())).into(holder.coverView);
        holder.itemView.setOnClickListener(v -> {
            if (onToPhotoListener != null) {
                onToPhotoListener.onClick(bucket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buckets.size();
    }

    public static class BucketHolder extends RecyclerView.ViewHolder {

        ImageView coverView;

        TextView bucketName;

        public BucketHolder(@NonNull View itemView) {
            super(itemView);
            coverView = itemView.findViewById(R.id.coverView);
            bucketName = itemView.findViewById(R.id.bucketName);
        }
    }


    public interface OnToPhotoListener {
        void onClick(Bucket bucket);
    }

}
```
5. 在Fragment中创建并实例化HomeAdapter，并在RecyclerView中设置布局管理器和适配器。  
GridLayoutManager是网格布局管理器，它的构造方法需要传入两个参数，第一个是当前上下文，第二个是每一行显示的列数。  
```
List<Bucket> buckets = BucketUtil.getBuckets(requireActivity());
if (buckets.size() > 0) {
    // 实例化适配器
    HomeAdapter homeAdapter = new HomeAdapter(buckets, bucket -> {
        Bundle bundle = new Bundle();
        bundle.putInt("BucketId",bucket.getBucketId());
        bundle.putString("BucketName",bucket.getBucketName());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_homeFragment_to_bucketFragment,bundle);
    });
    // 设置布局管理器
    binding.recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(),2));
    // 设置适配器
    binding.recyclerView.setAdapter(homeAdapter);
}
```
这样我们的第一个界面就完成了。
### 相册内容页
<img src="https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/51558f7ec8a34b7abc8de0698abcb87b~tplv-k3u1fbpfcp-watermark.image?" alt="origin_img_v2_55b651b9-8651-4a70-8f3e-dd17ff50930g.jpg" width="30%" />  

可以看到，这个界面和我们的首页是完全一样的，除了每一行的列数以及每个item的视图有一点区别。这里我们还是选择RecyclerView + Adapter的方式。  
1. 在相册内容页的布局文件中加入RecyclerView  
2. 可以看到图中的每个图片都是一个item，我们还需要创建一个布局文件去实现item的布局代码。 
3. 将数据显示到列表中，我们需要创建一个Adapter,对于RecyclerView我们还需要创建一个Holder，当然还需要定义了一个接口用于设置点击事件。
4. 在Fragment中创建并实例化Adapter，并在RecyclerView中设置布局管理器和适配器。  

这个页面基本上没有什么变化，所以我也就不贴代码了，当然文末有github链接，可以去下载源码。
### 大图页面
<img src="https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ae16cadcf3a54c9eaeebfd77d86c71d9~tplv-k3u1fbpfcp-watermark.image?" alt="origin_img_v2_53d66959-e384-46cf-be2c-595e3d88b3cg.jpg" width="30%" />

其实这个页面的布局很简单，就是一张图片，难点在于我们需要滑动来查看上一张，下一张图片，这里我采用ViewPager2 + Adapter的方式。  
1. 在相册页面的布局文件中加入ViewPager2。  
ViewPager2是一个滑动布局，**支持横向滑动以及竖向滑动**。
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".view.photo.PhotoFragment">

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager2"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</FrameLayout>
``` 
2. 准备一个Fragment以及对应的布局文件去显示我们的大图。 
```
public class PictureFragment extends Fragment {

    private final String path;

    private FragmentPictureBinding binding;

    public PictureFragment(String path) {
        this.path = path;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPictureBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Glide.with(requireActivity()).load(new File(path)).into(binding.imageView);
    }


}
```
```
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".view.photo.PhotoFragment">


    <ImageView
        android:id="@+id/imageView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```
3. 创建一个Adapter继承于FragmentStateAdapter,并重写相关方法。
```
public class ViewPagerAdapter extends FragmentStateAdapter {

    private final ArrayList<String> photos;

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, ArrayList<String> photos) {
        super(fragmentManager, lifecycle);
        this.photos = photos;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new PictureFragment(photos.get(position));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

}
```
4. 接收上一个页面传递过来的数据：图片数据、当前选中的图片位置。
```
if (getArguments() != null) {
    photos = getArguments().getStringArrayList("photos");
    cur = getArguments().getInt("cur");
}
```
5. 在Fragment中创建并实例化Adapter，并给ViewPager2设置适配器。  
```
ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(requireActivity().getSupportFragmentManager(), getLifecycle(), photos);
// 给viewPager2控件设置适配器
binding.viewPager2.setAdapter(viewPagerAdapter);
binding.viewPager2.setPageTransformer(new ZoomOutPageTransformer());
new Handler().post(() -> {
    binding.viewPager2.setCurrentItem(cur);
});
```
## 3. 图片加载
通过上述的操作，我们就完成了我们的简单图库App，当然图片加载是一个耗时的操作，所以我在加载图片的时候也使用到了一个[图片加载框架-Glide](https://github.com/bumptech/glide)，如果让图片都在主线程加载是会产生卡顿的，而且一些大图如果没有经过压缩等操作，还可能会导致内存溢出，但图片加载框架帮我们解决了这些问题，当然有能力的同学也可以去了解Glide框架的原理。  
# 四、总结
虽然是一个简单的图库App，但用到的东西挺多的。
- 组件：Activity、Fragment、ContentResolver
- UI控件：FragmentContainerView、ImageView、TextView、RecyclerView、ViewPager2等。
- 其它：动态申请权限、Glide图片加载框架等。

有点小伙伴会发现，其实我写的这个图库App只是一个粗略的版本，真正的图库App还有很多地方可以去优化。  
- 数据持久化：目前通过内容提供者获取的数据都只是存储在内存中，我们需要将它存储在我们的数据库，这样方便我们进行一些其它操作。 -> 对应android的数据存储(SQLite)
- 查看图片具体消息：我目前就是查询了图片的路径，其实还可以查询图片的更多消息：比如图片大小、创建时间等等。
- 列表分页显示，实现上拉加载更多，下拉刷新功能。
- 当然还有更多优化的点，这需要我们学习更多的知识。

# 五、项目源码

这是本项目的github链接：[图库 App 1.0](https://github.com/2441807818/mygallery)，可以star一下。

<img src="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/16066084a27a435a8684e99fd9345b8e~tplv-k3u1fbpfcp-watermark.image?" alt="image.png" width="50%" />

- Bucket：相册信息实体类  
- util：获取相册列表、相册图片列表的工具类
- bucket文件夹：相册内容页
- home文件夹：首页
- photo文件夹：大图页  
其中ZoomOutPageTransformer是[缩小页面转换器](https://developer.android.google.cn/training/animation/screen-slide-2?hl=zh_cn#zoom-out)

# 六、结语
如果喜欢或有所帮助的话，希望能点赞关注，鼓励一下作者。  
如果文章有不正确或存疑的地方，欢迎评论指出。
