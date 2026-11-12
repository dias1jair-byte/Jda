package br.com.jdareparos.vistorias;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Base64;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_CHOOSER_REQUEST = 4102;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("file".equals(uri.getScheme()) || "about".equals(uri.getScheme())) return false;
                if ("https".equals(uri.getScheme()) && "wa.me".equals(uri.getHost())) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
                    return true;
                }
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = params.createIntent();
                try { startActivityForResult(intent, FILE_CHOOSER_REQUEST); }
                catch (ActivityNotFoundException e) { fileCallback = null; Toast.makeText(MainActivity.this, "Não foi possível abrir os arquivos do aparelho.", Toast.LENGTH_LONG).show(); return false; }
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && fileCallback != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    private Uri writeSharedFile(String name, byte[] bytes) throws IOException {
        File dir = new File(getCacheDir(), "shared");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Não foi possível criar a pasta temporária");
        String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
        File file = new File(dir, safe);
        try (FileOutputStream out = new FileOutputStream(file)) { out.write(bytes); out.flush(); }
        return FileProvider.getUriForFile(this, "br.com.jdareparos.vistorias.fileprovider", file);
    }

    private void shareFile(Uri uri, String mime, String title, String message) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType(mime);
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.putExtra(Intent.EXTRA_SUBJECT, title);
        send.putExtra(Intent.EXTRA_TEXT, message);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try { startActivity(Intent.createChooser(send, "Compartilhar com")); }
        catch (Exception e) { Toast.makeText(this, "Não foi possível abrir o compartilhamento.", Toast.LENGTH_LONG).show(); }
    }

    public class AndroidBridge {
        @JavascriptInterface public void sharePdf(String base64, String name, String message, String title) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Uri uri = writeSharedFile(name.toLowerCase().endsWith(".pdf") ? name : name + ".pdf", bytes);
                    shareFile(uri, "application/pdf", title, message);
                } catch (Exception e) { Toast.makeText(MainActivity.this, "Falha ao preparar o PDF: " + e.getMessage(), Toast.LENGTH_LONG).show(); }
            });
        }

        @JavascriptInterface public void shareBackup(String json) {
            runOnUiThread(() -> {
                try {
                    Uri uri = writeSharedFile("backup-jda-vistorias.json", json.getBytes("UTF-8"));
                    shareFile(uri, "application/json", "Backup JDA Vistorias", "Cópia de segurança das vistorias JDA.");
                } catch (Exception e) { Toast.makeText(MainActivity.this, "Falha ao preparar o backup.", Toast.LENGTH_LONG).show(); }
            });
        }

        @JavascriptInterface public void printHtml(String html) {
            runOnUiThread(() -> {
                WebView printWeb = new WebView(MainActivity.this);
                printWeb.getSettings().setJavaScriptEnabled(false);
                printWeb.loadDataWithBaseURL("file:///android_asset/", "<html><head><meta charset='utf-8'><style>body{font-family:Arial,sans-serif;color:#17221b;padding:18px}h1{font-size:22px;color:#176b3a}h2{font-size:16px;color:#173126;margin-top:18px}.meta{display:grid;grid-template-columns:1fr 1fr;gap:7px;margin:12px 0}.meta div,.report-item{border:1px solid #d9e2dc;padding:8px;margin:7px 0;break-inside:avoid}.report-item img{max-width:100%;max-height:240px;object-fit:contain}.signatures{display:grid;grid-template-columns:1fr 1fr;gap:35px;margin-top:55px}.signatures div{border-top:1px solid #333;text-align:center;padding-top:6px}.ref{font-weight:bold;background:#e8f0eb;padding:8px}</style></head><body>" + html + "</body></html>", "text/html", "UTF-8");
                printWeb.setWebViewClient(new WebViewClient() {
                    @Override public void onPageFinished(WebView view, String url) {
                        PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                        PrintDocumentAdapter adapter = view.createPrintDocumentAdapter("JDA-Vistoria");
                        pm.print("JDA Vistoria", adapter, new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).setResolution(new PrintAttributes.Resolution("jda", "JDA", 300, 300)).setMinMargins(PrintAttributes.Margins.NO_MARGINS).build());
                    }
                });
            });
        }
    }
}
