package jp.kossacktouch.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.util.Size;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.core.resolutionselector.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import jp.kossacktouch.core.CameraAnswer;
import java.util.concurrent.*;

/** Live preview/analysis only. No still capture, files, clipboard, or LINE actions. */
public final class CameraActivity extends Activity implements LifecycleOwner {
 private final Handler main=new Handler(Looper.getMainLooper());
 private final LifecycleRegistry lifecycle=new LifecycleRegistry(this);
 private final ExecutorService frames=Executors.newSingleThreadExecutor();
 private PreviewView preview;private TextView answer,detail;private ProcessCameraProvider provider;
 private TextRecognizer recognizer;private CameraAnswer solver;
 private volatile boolean resumed,busy,destroyed;private volatile int generation;
 private long lastFrame;private long lastResult;private boolean permissionAsked,opening;
 private final Runnable expiry=new Runnable(){public void run(){if(resumed&&!destroyed){if(lastResult>0&&SystemClock.elapsedRealtime()-lastResult>2500){showError("文字を確認できません。問題全文を映してください。");lastResult=0;}main.postDelayed(this,500);}}};
 @NonNull @Override public Lifecycle getLifecycle(){return lifecycle;}
 @Override public void onCreate(Bundle state){super.onCreate(state);lifecycle.setCurrentState(Lifecycle.State.CREATED);
  if(QuizService.instance!=null)QuizService.instance.stop("停止","カメラモード（LINE自動操作は停止中）");
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(17,40,51));
  root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
  Button back=new Button(this);back.setText("‹ 通常モードへ戻る（停止状態）");back.setOnClickListener(v->finish());root.addView(back,new LinearLayout.LayoutParams(-1,dp(48)));
  TextView guide=new TextView(this);guide.setText("カメラモード · 第○問から問題全文まで、1問だけ映してください。第1〜4問はA〜Dも映してください。");guide.setTextColor(Color.WHITE);guide.setTextSize(14);guide.setPadding(dp(12),dp(8),dp(12),dp(8));root.addView(guide);
  preview=new PreviewView(this);preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);preview.setScaleType(PreviewView.ScaleType.FIT_CENTER);root.addView(preview,new LinearLayout.LayoutParams(-1,0,1));
  answer=new TextView(this);answer.setText("—");answer.setTextColor(Color.rgb(245,203,105));answer.setTextSize(40);answer.setGravity(Gravity.CENTER);root.addView(answer);
  detail=new TextView(this);detail.setTextColor(Color.WHITE);detail.setTextSize(14);detail.setGravity(Gravity.CENTER);detail.setPadding(dp(12),dp(6),dp(12),dp(12));detail.setText("写真は保存しません · 回答操作は本人が行います");root.addView(detail);setContentView(root);
  try{solver=new CameraAnswer(Store.bank(this));recognizer=TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());}catch(Exception e){showError("認識機能を初期化できません");}
 }
 @Override protected void onResume(){super.onResume();resumed=true;generation++;lifecycle.setCurrentState(Lifecycle.State.RESUMED);showError("第○問と問題全文を映してください（1秒ごと・2回一致）");main.post(expiry);
  if(checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)openCamera();
  else if(!permissionAsked){permissionAsked=true;requestPermissions(new String[]{Manifest.permission.CAMERA},71);}
 }
 @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){super.onRequestPermissionsResult(request,permissions,grants);
  if(request==71){if(grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)openCamera();else showError("カメラ権限が必要です。通常モードはそのまま利用できます。");}
 }
 @Override protected void onPause(){resumed=false;generation++;opening=false;main.removeCallbacks(expiry);if(provider!=null)provider.unbindAll();lifecycle.setCurrentState(Lifecycle.State.CREATED);showError("カメラ停止中");super.onPause();}
 @Override protected void onDestroy(){destroyed=true;main.removeCallbacks(expiry);if(provider!=null)provider.unbindAll();lifecycle.setCurrentState(Lifecycle.State.DESTROYED);frames.shutdown();if(!busy&&recognizer!=null){recognizer.close();recognizer=null;}super.onDestroy();}
 @Override public void finish(){if(QuizService.instance!=null){QuizService.instance.showFloat();QuizService.instance.stop("停止","カメラモード終了 · ▶で再開してください");}super.finish();}
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private void showError(String message){if(solver!=null)solver.reset();if(answer!=null)answer.setText("—");if(detail!=null)detail.setText(message);}
 private void openCamera(){
  if(!resumed||destroyed||opening||recognizer==null||checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)return;
  opening=true;final int ticket=generation;
  ListenableFuture<ProcessCameraProvider> future=ProcessCameraProvider.getInstance(this);
  future.addListener(()->{if(ticket!=generation||!resumed||destroyed)return;opening=false;
   try{
    provider=future.get();if(!provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)){showError("背面カメラを利用できません");return;}
    ResolutionSelector resolution=new ResolutionSelector.Builder().setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY).setResolutionStrategy(new ResolutionStrategy(new Size(1920,1080),ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)).build();
    Preview live=new Preview.Builder().setResolutionSelector(resolution).build();live.setSurfaceProvider(preview.getSurfaceProvider());
    ImageAnalysis analysis=new ImageAnalysis.Builder().setResolutionSelector(resolution).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();analysis.setAnalyzer(frames,this::frame);
    provider.unbindAll();provider.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,live,analysis);
   }catch(Exception e){showError("カメラを開けません（他アプリの使用・端末設定を確認）");}
  },command->main.post(command));
 }
 @androidx.annotation.OptIn(markerClass=ExperimentalGetImage.class)
 private void frame(ImageProxy image){
  long now=SystemClock.elapsedRealtime();if(!resumed||destroyed||busy||now-lastFrame<1000||image.getImage()==null){image.close();return;}
  lastFrame=now;busy=true;final int ticket=generation;final long captured=now;
  try{recognizer.process(InputImage.fromMediaImage(image.getImage(),image.getImageInfo().getRotationDegrees()))
   .addOnSuccessListener(text->{if(ticket==generation&&resumed&&!destroyed){long received=SystemClock.elapsedRealtime();if(received-captured>2500){showError("認識待ちです。問題を動かさず映してください。");return;}lastResult=received;CameraAnswer.Reading result=solver.observe(text.getText(),captured);answer.setText(result.answer.isEmpty()?"—":result.answer);detail.setText(result.detail);}})
   .addOnFailureListener(e->{if(ticket==generation&&resumed&&!destroyed)showError("文字を読み取れません。距離・反射・ピントを調整してください。");})
   .addOnCompleteListener(task->{image.close();busy=false;if(destroyed&&recognizer!=null){recognizer.close();recognizer=null;}});
  }catch(Exception e){image.close();busy=false;main.post(()->{if(ticket==generation&&!destroyed)showError("文字認識を開始できません");});}
 }
}
