package com.example.ConnectExistingSQLiteDatabase.lu101701;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MainActivity extends AppCompatActivity {
    EditText name,tel,address,et,updatename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name=(EditText)findViewById(R.id.name);
        tel=(EditText)findViewById(R.id.tel);
        address=(EditText)findViewById(R.id.address);
        et=(EditText)findViewById(R.id.et);
        updatename=(EditText)findViewById(R.id.updateName);
    }
    //copy　sqllite檔案到手機方法一
    public void copy(View view) {
        //要取得專案中的resources，就要用getResources()，回傳Resources物件
        //要抓raw中的資料，要用openRawResource，回傳InputStream物件，再用一個InputStream變數接住
        InputStream is=getResources().openRawResource(R.raw.student);
        try {
            //new一個FileOutputStream物件(FileOutputStream是OutputStream的子類別)
            //輸出串流有很多種，網路資料串流，文字資料串流...
            OutputStream os=new FileOutputStream(getFilesDir()+ File.separator+"student.sqlite");
            //從inputStream讀，從OutputStream寫出去
            int i=0;
            //每讀一個byte進來就寫一個byte，當i=-1時讀不到就結束
            while(i!=-1){
                i=is.read();
                os.write(i);
            }
            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //copy　sqllite檔案到手機方法二
    //用java nio2的path(java7以後才有)，用path或Files的method來做
    public void copy2(View view) {
        //因為path和Files要用api26以上測試，所以不能用android device monitor看檔案是否建立
        //要用File+log來看
        File f = new File(getFilesDir() + File.separator + "student2.sqlite");
        //boolean exists ()
        //Tests whether the file or directory denoted by this abstract pathname exists.
        // return:true if and only if the file or directory denoted by this abstract pathname exists; false otherwise
        //先偵測檔案是否存在，不存在才覆蓋檔案過去
        if(!f.exists()){
            InputStream is = getResources().openRawResource(R.raw.student);
            //uri抓file的格式是file:///tmp/android.txt，而getFilesDir().getAbsolutePath()取出來前面會有一個斜線，所以file:後面只要兩條斜線
            //create():URI create (String str)
            //Creates a URI by parsing the given string.
            URI uri = URI.create("file://" + getFilesDir().getAbsolutePath() + File.separator + "student2.sqlite");
            //Path get (URI uri)
            //Converts the given URI to a Path object.
            Path p = Paths.get(uri);
            try {
                //long copy (InputStream in, Path target,CopyOption... options)
                //Copies all bytes from an input stream to a file.
                //CopyOption: options specifying how the copy should be done
                //三個參數是inputStream,paht,常數
                //StandardCopyOption:Defines the standard copy options.
                //REPLACE_EXISTING:Replace an existing file if it exists.
                Files.copy(is, p, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("FILE", String.valueOf(f.exists()));
    }
    //讀取sqlLite檔
    public void clickRead(View view) {
        String path = getFilesDir().getAbsolutePath() + File.separator + "student2.sqlite";
        /*SQLiteDatabase openDatabase (String path, SQLiteDatabase.CursorFactory factory, int flags)
        Open the database according to the flags OPEN_READWRITE(0) OPEN_READONLY(1) CREATE_IF_NECESSARY(268435456) and/or NO_LOCALIZED_COLLATORS(16).
        Parameters:
        path=>String: to database file to open and/or create
        factory	SQLiteDatabase.CursorFactory: an optional factory class that is called to instantiate a cursor when query is called, or null for default
        flags=>int: to control database access mode
        Returns：
        SQLiteDatabase	the newly opened database */
        //宣告db是在抓資料庫student的資料
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, 0);
        //Cusor:This interface provides random read-write access to the result set returned by a database query.
        //執行sql時，可以直接用execSQL，下SQL指令，或是用query，sqlLite內建的方法
        //Query the given URL, returning a Cursor over the result set.有七個參數，後面五個可以用null，前兩個分別為table name，用字串陣列存的欄位名稱
        //注意欄位名稱不可以和sql指令重複ex:add不可用
        //宣告c是在抓資料庫裡一筆一筆的資料，cursor是代表指向第幾筆資料
        Cursor c = db.query("phone", new String[] {"id", "name", "tel", "addr"}, null, null, null, null, null);
        //因為cursor沒有設定的話會指向第一筆資料的上面空白出，所以設定移動到第一筆資料
        c.moveToFirst();
        //Log.d("COUNT", "NUMBER"+c.getCount());
        StringBuilder sb=new StringBuilder();
        do{
            sb.append(String.valueOf(c.getInt(0)));
            sb.append(":");
            //Log.d("DB", String.valueOf(c.getInt(0)));//用log print id
            for(int i=1;i<4;i++){
                sb.append(c.getString(i)+",");
                //Log.d("DB", c.getString(i)); //getString(i)取得第i筆的欄位資料
            }
            sb.append("\n");
            et.setText(sb.toString());
            //Log.d("DB", c.getString(1));
            //moveToNext():Move the cursor to the next row.
        }while (c.moveToNext());

    }
    //新增SQL lite資料
    public void clicknew(View view) {
        String path = getFilesDir().getAbsolutePath() + File.separator + "student2.sqlite";
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, 0);
        //ContentValues:This class is used to store a set of values that the ContentResolver can process.
        //ContentValues也是map的概念，put()兩個參數，key和value，key就是欄位名稱
        ContentValues cv = new ContentValues();
        cv.put("name",name.getText().toString());
        cv.put("tel",tel.getText().toString());
        cv.put("addr",address.getText().toString());
        //long insert (String table, String nullColumnHack, ContentValues values)=>因為插入的內容比須是一個ContentValues，所以前面要先把ContentValues建好
        //Convenience method for inserting a row into the database.
        db.insert("phone", null, cv);
        db.close();

    }
    public void clear(View view) {
        name.setText("");
        tel.setText("");
        address.setText("");
    }
    //刪除table欄位資料
    public void delete(View view) {
        String path = getFilesDir().getAbsolutePath() + File.separator + "student2.sqlite";
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        /*delete
        int delete (String table,String whereClause, String[] whereArgs)
        Convenience method for deleting rows in the database.
        參數說明：
        1.table name
        2.要如何篩選資料,寫法:id=? 代表要把id都是?的資料都找出來，同時對應到下個參數為指定數值的資料
        =>如果有兩個不同欄位寫法為id=? and tel=? ，同時陣列的參數就要變成new String[] {"2","2"}，指定不同欄位的第幾筆資料
        =>如果同一個欄位要刪除多筆資料寫法範例：id IN (?,?,?)
        3.這個參數是陣列型態*/
        //注意三四個參數絕對不能NULL，否則會把所有的資料全部刪除
        db.delete("phone", "id IN (?,?)", new String[] {"2","5"});
        db.close();
    }
    //更新table欄位資料
    public void update(View view) {
        String path = getFilesDir().getAbsolutePath() + File.separator + "student2.sqlite";
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        ContentValues cv = new ContentValues();
        cv.put("name", updatename.getText().toString());
        //int update (String table, ContentValues values, String whereClause, String[] whereArgs)
        //Convenience method for updating rows in the database.
        //參數說明：1.資料表名稱 2.要更新的資料
       //注意三四個參數絕對不能NULL，否則會把所有的資料全部更新
        db.update("phone", cv, "id=?", new String[] {"1"});
        db.close();
    }
}
