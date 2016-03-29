package com.ex.ak.productexpandlist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity
{
    private final static String ELV_PARENT_KEY          = "parentKey";        // Key for PARENT Groups
    private final static String ELV_CHILD_KEY_NAME      = "childKeyName";     // Key for child Groups - product Name
    private final static String ELV_CHILD_KEY_PRICE     = "childKeyPrice";    // Key for child Groups - product Price
    private final static String ELV_CHILD_KEY_WEIGHT    = "childKeyWeight";   // Key for child Groups - product Weight

    private ArrayList<View> allViews = new ArrayList<>();
    private int curGroupItem = -1;
    private int curChildItem = -1;

    private ExpandableListView ELV;		       		                            // ExpandableListView - list of categories and items belonging to them
    private SimpleExpandableListAdapter adapter;	                            // Adapter for this List

    private ArrayList<Map<String,String>>            parentNodes = new ArrayList<>();   // List of parent Nodes
    private ArrayList<ArrayList<Map<String,String>>> childNodes  = new ArrayList<>();   // List Child Nodes

    private View dialogViewAddUpd;              	                            //  Dialog View
    private AlertDialog.Builder biulder;        	                            //  builder for Add && Update Dialog
    private LayoutInflater inflater;
    private EditText productName;
    private EditText productPrice;
    private EditText productWeight;
    private Spinner spinnerCategory;
    private ArrayAdapter<String> adapterCategory;                               // Adapter used for Spinner Category
    private static boolean isUpdate = false;		                            // if Action in menu != Update
    private static Product tmp;

    private static File ExtStorDir = Environment.getExternalStorageDirectory(); // ---- getting path to ExternalStorageDirectory
    private static File F = new File(ExtStorDir,"products.txt");				// ---- creating file
    private ArrayList<Product> allProducts = new ArrayList<>();					// ---- filling child Groups


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this.fillCollectionOfProducts();

        /**
         * 	------ restore from File ---------------
         */
        try
        {
            FileInputStream   FIS   = new FileInputStream(F);
            ObjectInputStream OIS   = new ObjectInputStream(FIS);

            if(FIS.available() > 0 )
            {
                while(FIS.available() > 0)
                {
                    Product tmpProduct = (Product)OIS.readObject();

                    if(tmpProduct instanceof Product)
                    {
                        allProducts.add(tmpProduct);
                    }
                    else
                    {
                        System.out.println("Object not initialised " + tmpProduct.getClass().getName());
                    }
                }
                OIS.close();
            }
        }
        catch (IOException e)
        {
            System.out.println("Error : " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }


        /**
         *	------  filling parent Groups -------------------------
         */
        String[] arrGroups = {"Fruits" , "Chocos" , "Beverages"};

        for(int i = 0 ; i< arrGroups.length ; i++)
        {
            HashMap<String,String> HM = new HashMap<>();
            HM.put(MainActivity.ELV_PARENT_KEY, arrGroups[i]);
            this.parentNodes.add(HM);
        }

        /**
         *	------  initializing dialog Window && etc ---------------
         */
        this.biulder            = new AlertDialog.Builder(this);
        this.inflater           = this.getLayoutInflater();
        this.dialogViewAddUpd   = inflater.inflate(R.layout.dialog_maket, null, false);
        this.productName        = (EditText)this.dialogViewAddUpd.findViewById(R.id.etName);
        this.productPrice       = (EditText)this.dialogViewAddUpd.findViewById(R.id.etPrice);
        this.productWeight      = (EditText)this.dialogViewAddUpd.findViewById(R.id.etWeight);
        this.spinnerCategory    = (Spinner)this.dialogViewAddUpd.findViewById(R.id.spnCategory);
        this.adapterCategory    = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrGroups);
        this.spinnerCategory.setAdapter(this.adapterCategory);
        this.biulder.setView(this.dialogViewAddUpd);

        this.ELV = (ExpandableListView)this.findViewById(R.id.elvOne);

        /**
         *	------  filling ExpandableListView ----------------
         */
        for(int i = 0; i < parentNodes.size() ;i++)
        {
            ArrayList<Map<String,String>> childNode = new ArrayList<>(); // -- Collection of child products for Current Category

            for(int j = 0 ; j < allProducts.size(); j++)
            {
                if(allProducts.get(j).idCategory != i) continue;
                /**
                 *	if Product found in current Category - add this Product
                 */
                Product t = allProducts.get(j).cloneProduct();
                HashMap<String,String> HM = new HashMap<>();
                HM.put(MainActivity.ELV_CHILD_KEY_NAME,t.name);
                HM.put(MainActivity.ELV_CHILD_KEY_PRICE, t.price + "");
                HM.put(MainActivity.ELV_CHILD_KEY_WEIGHT, t.weight + "");
                childNode.add(HM);
            }
            this.childNodes.add(childNode);
        }

        String[] parentNodeFrom = {MainActivity.ELV_PARENT_KEY};
        int[]    parentNodeTo   = {android.R.id.text1};

        String[] childNodeFrom = {  MainActivity.ELV_CHILD_KEY_NAME, MainActivity.ELV_CHILD_KEY_PRICE, MainActivity.ELV_CHILD_KEY_WEIGHT };
        int[]    childNodeTo   = {      R.id.tvName,                         R.id.tvPrice,                          R.id.tvWeight      };

        /**
         *	 Adapter for ExpandableListView
         */
        this.adapter = new SimpleExpandableListAdapter(
                this,
                this.parentNodes,
                android.R.layout.simple_expandable_list_item_1,
                parentNodeFrom,
                parentNodeTo,
                this.childNodes,
                R.layout.child_item,
                childNodeFrom,
                childNodeTo)
        {
            @Override
            public View getChildView(   int groupPosition,
                                        int childPosition,
                                        boolean isLastChild,
                                        View convertView,
                                        ViewGroup parent)
            {
                View view = super.getChildView(groupPosition,childPosition,isLastChild,convertView,parent);
                CheckBox cb = (CheckBox)view.findViewById(R.id.cb1);

                if(MainActivity.this.curGroupItem == groupPosition && MainActivity.this.curChildItem == childPosition)
                {
                    // --  Setting Item Selected -
                    cb.setChecked(true);
                    view.setBackgroundColor(Color.CYAN);
                }
                else
                {
                    // --  DisSelect Item  -
                    cb.setChecked(false);
                    view.setBackgroundColor(Color.rgb(0xe4,0xe2,0x84));
                }

                // -- remember Link to Vidjet to collection -
                if(MainActivity.this.allViews.contains(view) == false)
                {
                    MainActivity.this.allViews.add(view);
                }
                return view;
            }
        };

        /**
         *	Event Handler
         */
        this.ELV.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id)
            {
                // --  set all Vidgets color -
                for(View V : MainActivity.this.allViews )
                {
                    CheckBox cbA = (CheckBox) V.findViewById(R.id.cb1);
                    cbA.setChecked(false);
                    V.setBackgroundColor(Color.rgb(0xe4,0xe2,0x84));
                }

                // -- remember current selected element -
                MainActivity.this.curGroupItem = groupPosition;
                MainActivity.this.curChildItem = childPosition;

                // -- setting flag to Checkbox -
                CheckBox cb = (CheckBox)view.findViewById(R.id.cb1);
                cb.setChecked(true);

                // -- current Vidget - element of list is highlighted  -
                view.setBackgroundColor(Color.rgb(0x93,0xca,0xf1));
                return true;
            }
        });

        /**
         *	Actions of Positive & Negative buttons of dialog
         */
        this.biulder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int id)
            {
                String name     = MainActivity.this.productName.getText().toString();
                String price    = MainActivity.this.productPrice.getText().toString();
                String weight   = MainActivity.this.productWeight.getText().toString();

                HashMap<String,String> tmpProduct = new HashMap<>();

                tmpProduct.put(ELV_CHILD_KEY_NAME,name);
                tmpProduct.put(ELV_CHILD_KEY_PRICE,price);
                tmpProduct.put(ELV_CHILD_KEY_WEIGHT,weight);

                int curGroupNew = spinnerCategory.getSelectedItemPosition();     // -- selected category in spinner
                ArrayList<Map<String,String>> temp ;

                if(!isUpdate)
                {
                    temp = childNodes.get(curGroupNew);
                    temp.add(tmpProduct);
                    childNodes.add(temp);
                }
                else
                {
                    if(curGroupNew != curGroupItem)	 						    // -- if categories of Product is not match -
                    {
                        childNodes.get(curGroupItem).remove(curChildItem);	    // -- from old category -
                        childNodes.get(curGroupNew).add(tmpProduct);		    // -- to new category -
                    }
                    else
                    {
                        childNodes.get(curGroupNew).remove(curChildItem);
                        childNodes.get(curGroupNew).add(curChildItem,tmpProduct);
                    }
                }
                productName.setText("");
                productPrice.setText("");
                productWeight.setText("");
                adapter.notifyDataSetChanged();
                ((ViewGroup)(dialogViewAddUpd.getParent())).removeAllViews();
            }
        });

        this.biulder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int id)
            {
                ((ViewGroup)(dialogViewAddUpd.getParent())).removeAllViews();
            }
        });

        // -- setting Adapter to ELV -
        this.ELV.setAdapter(adapter);
    }


    /**
     *   ----- check for Writing to external storage
     */
    private boolean isExstStorageAvailableForWriting()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /**
     *	------ save to File ----------------
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        try
        {
            if(isExstStorageAvailableForWriting())
            {
                FileOutputStream    FOS = new FileOutputStream(F);
                ObjectOutputStream  OOS = new ObjectOutputStream(FOS);

                // ------  for all categories ------------------------------------
                for(int i = 0; i< childNodes.size() ;i++)
                {
                    //----- for all Products -------------------------------------
                    ArrayList<Map<String , String>> listOfCategory = childNodes.get(i);

                    for(int j = 0 ; j < listOfCategory.size(); j++)
                    {
                        Map<String , String> HashMapProd = listOfCategory.get(j);

                        String ProductName  = HashMapProd.get(ELV_CHILD_KEY_NAME);
                        String ProductPrice = HashMapProd.get(ELV_CHILD_KEY_PRICE);
                        String ProductWeigt = HashMapProd.get(ELV_CHILD_KEY_WEIGHT);

                        Product t = new Product(ProductName,Double.parseDouble(ProductPrice),Integer.parseInt(ProductWeigt),i);
                        OOS.writeObject(t);
                    }
                }
                OOS.flush();
                OOS.close();
            }
        }
        catch (IOException e)
        {
            System.out.println("Error writing to File : " + e.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        switch(id)
        {
            // -- A D D -
            case R.id.action_add :

                this.biulder.setView(this.dialogViewAddUpd);
                this.biulder.setTitle("Add Product");
                this.spinnerCategory.setAdapter(this.adapterCategory);
                isUpdate = false;
                AlertDialog dialog1 = biulder.create();
                dialog1.show();

                return true;

            // -- U P D A T E -
            case R.id.action_upd :

                isUpdate     = true;
                this.biulder.setView(this.dialogViewAddUpd);
                this.biulder.setTitle("Update Product");

                if(curChildItem != -1)
                {
                    HashMap<String,String> tmpProduct = new HashMap<>();
                    tmpProduct.putAll(childNodes.get(curGroupItem).get(curChildItem));
                    String n = tmpProduct.get(ELV_CHILD_KEY_NAME);
                    String p = tmpProduct.get(ELV_CHILD_KEY_PRICE);
                    String w = tmpProduct.get(ELV_CHILD_KEY_WEIGHT);

                    // -- set Old Name to EditText Field -
                    this.productName.setText(n);
                    // -- set Old Price to EditText Field -
                    this.productPrice.setText(p);
                    // -- set Old Weight to EditText Field -
                    this.productWeight.setText(w);
                    // -- set Spinner to Current Category -
                    this.spinnerCategory.setSelection(curGroupItem);

                    AlertDialog dialog2 = biulder.create();
                    dialog2.show();
                }
                else
                {
                    Toast.makeText(this,"Item of list not selected !", Toast.LENGTH_SHORT).show();
                }
                return true;


            // -- D E L E T E -
            case R.id.action_del :

                if(curChildItem != -1)
                {
                    AlertDialog.Builder biulder = new AlertDialog.Builder(this);
                    biulder.setTitle("Delete Product");
                    biulder.setMessage("Confirm Delete ?");
                    biulder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            childNodes.get(curGroupItem).remove(curChildItem);
                            curChildItem = -1;
                            curGroupItem = -1;
                            adapter.notifyDataSetChanged();
                        }
                    });
                    biulder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    AlertDialog dialog3 = biulder.create();
                    dialog3.show();
                }
                else
                {
                    Toast.makeText(this,"Product not Selected",Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void fillCollectionOfProducts()
    {
         allProducts.add(new Product("Snickers",    4.75,   45,     Product.CATEGORY_CHOCO));
         allProducts.add(new Product("Mars",        5.15,   50,     Product.CATEGORY_CHOCO));
         allProducts.add(new Product("CocaCola",    9.90,   1000,   Product.CATEGORY_BEVERAGES));
         allProducts.add(new Product("Apple",       18.50,  1000,   Product.CATEGORY_FRUITS));
         allProducts.add(new Product("Orange",      45.00,  1000,   Product.CATEGORY_FRUITS));
         allProducts.add(new Product("Bounty",      8.75,   80,     Product.CATEGORY_CHOCO));
         allProducts.add(new Product("Fanta",       11.30,  500,    Product.CATEGORY_BEVERAGES));
         allProducts.add(new Product("Beer",        14.30,  500,    Product.CATEGORY_BEVERAGES));
    }
}
