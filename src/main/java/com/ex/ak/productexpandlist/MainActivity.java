package com.ex.ak.productexpandlist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity
{
    /**
     *  Key for PARENT Groups
     */
    private final static    String          ELV_PARENT_KEY          = "parentKey";    
    
    /**
     *  Key for child Groups - product Name
     */
    private final static    String          ELV_CHILD_KEY_NAME      = "childKeyName";   
    
    /**
     *  Key for child Groups - product Price
     */
    private final static    String          ELV_CHILD_KEY_PRICE     = "childKeyPrice"; 
    
    /**
     *  Key for child Groups - product Weight
     */
    private final static    String          ELV_CHILD_KEY_WEIGHT    = "childKeyWeight";   

    private ArrayList<View> allViews        = new ArrayList<>();
    private int             curGroupItem    = -1;
    private int             curChildItem    = -1;

    /**
     *  ExpandableListView - list of categories and items belonging to them
     */
    private ExpandableListView              ELV;	
    
    /**
     *  Adapter for ExpandableListView
     */
    private SimpleExpandableListAdapter      adapter;	                           

    /**
     *  List of parent Nodes
     */
    private ArrayList<Map<String,String>>               parentNodes = new ArrayList<>();  
    
    /**
     *  List Child Nodes
     */
    private ArrayList<ArrayList<Map<String,String>>>    childNodes  = new ArrayList<>(); 
    
    /**
     *  Filling child Groups
     */
    private ArrayList<Tovar>                            allTovars   = new ArrayList<>();		

    /**
     *  Dialog View for Action Ading the current Item & Action Update the current Item
     */
    private View                    dialogViewAddUpd;              	                              
    
    /**
     *  Builder for Dialog View for Action Adding the current Item & Action Update the current Item
     */
    private AlertDialog.Builder     biulder;        	                           
    private LayoutInflater          inflater;
    
    /**
     *  Fields o the Dialog View for Action Adding the current Item & Action Update the current Item
     */
    private EditText                tovarName;
    private EditText                tovarPrice;
    private EditText                tovarWeight;
    private Spinner                 spinnerCategory;
    
    /**
     *  Adapter used for Spinner Category
     */
    private ArrayAdapter<String>    adapterCategory;                              
    
    /**
     *  Boolean variable 
     *  returnes true if Action in menu == Action Update the current Item
     */
    private static boolean          isUpdate    = false;		                           
    private static Tovar            tmp;

    /**
     *  Path to ExternalStorageDirectory for saving data
     */
    private static File             ExtStorDir  = Environment.getExternalStorageDirectory(); 
    
    /**
     *  Creating file for saving data on ExternalStorageDirectory
     */
    private static File             F           = new File(ExtStorDir,"products.txt");			
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this.fillCollectionOfProducts();

        /**
         * 	Restore Data from File 
         */
        try
        {
            FileInputStream   FIS   = new FileInputStream(F);
            ObjectInputStream OIS   = new ObjectInputStream(FIS);

            if(FIS.available() > 0 )
            {
                while(FIS.available() > 0)
                {
                    Tovar tmpTovar = (Tovar)OIS.readObject();

                    if(tmpTovar instanceof Tovar)
                    {
                        allTovars.add(tmpTovar);
                    }
                    else
                    {
                        System.out.println("Object not initialised " + tmpTovar.getClass().getName());
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
         *	Filling parent Groups
         */
        String[] arrGroups = {"Fruits" , "Chocos" , "Beverages"};

        for(int i = 0 ; i< arrGroups.length ; i++)
        {
            HashMap<String,String> HM = new HashMap<>();
            HM.put(MainActivity.ELV_PARENT_KEY, arrGroups[i]);
            this.parentNodes.add(HM);
        }

        /**
         *	Initializing dialog view && etc
         */
        this.biulder            = new AlertDialog.Builder(this);
        this.inflater           = this.getLayoutInflater();
        this.dialogViewAddUpd   = inflater.inflate(R.layout.dialog_maket, null, false);
        this.tovarName          = (EditText)this.dialogViewAddUpd.findViewById(R.id.etName);
        this.tovarPrice         = (EditText)this.dialogViewAddUpd.findViewById(R.id.etPrice);
        this.tovarWeight        = (EditText)this.dialogViewAddUpd.findViewById(R.id.etWeight);
        this.spinnerCategory    = (Spinner)this.dialogViewAddUpd.findViewById(R.id.spnCategory);
        this.adapterCategory    = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrGroups);
        this.spinnerCategory.setAdapter(this.adapterCategory);
        this.biulder.setView(this.dialogViewAddUpd);

        this.ELV = (ExpandableListView)this.findViewById(R.id.elvOne);

        /**
         *	Filling ExpandableListView 
         */
        for(int i = 0; i < parentNodes.size() ;i++)
        {
            /**
             *  Collection of child products for Current Category
             */
            ArrayList<Map<String,String>> childNode = new ArrayList<>(); 

            for(int j = 0 ; j < allTovars.size(); j++)
            {
                if(allTovars.get(j).idCategory != i) continue;
                /**
                 *	if Product found in current Category - add this Product
                 */
                Tovar t = allTovars.get(j).cloneTovar();
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
                    // Setting Item Selected
                    cb.setChecked(true);
                    view.setBackgroundColor(Color.CYAN);
                }
                else
                {
                    // DisSelect Item
                    cb.setChecked(false);
                    view.setBackgroundColor(Color.rgb(0xe4,0xe2,0x84));
                }

                // remember Link to Vidjet to collection -
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
                // set color to all Vidgets 
                for(View V : MainActivity.this.allViews )
                {
                    CheckBox cbA = (CheckBox) V.findViewById(R.id.cb1);
                    cbA.setChecked(false);
                    V.setBackgroundColor(Color.rgb(0xe4,0xe2,0x84));
                }

                // remember current selected element 
                MainActivity.this.curGroupItem = groupPosition;
                MainActivity.this.curChildItem = childPosition;

                // setting flag to Checkbox 
                CheckBox cb = (CheckBox)view.findViewById(R.id.cb1);
                cb.setChecked(true);

                // current Vidget - element of list is highlighted 
                view.setBackgroundColor(Color.rgb(0x93,0xca,0xf1));
                return true;
            }
        });

        /**
         *	Actions for Positive & Negative buttons of dialog view
         */
        this.biulder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int id)
            {
                String name     = MainActivity.this.tovarName.getText().toString();
                String price    = MainActivity.this.tovarPrice.getText().toString();
                String weight   = MainActivity.this.tovarWeight.getText().toString();

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
                    // if categories of Product is not match 
                    if(curGroupNew != curGroupItem)	 						    
                    {
                         // from old category
                        childNodes.get(curGroupItem).remove(curChildItem);
                        // to new category 
                        childNodes.get(curGroupNew).add(tmpProduct);		    
                    }
                    else
                    {
                        childNodes.get(curGroupNew).remove(curChildItem);
                        childNodes.get(curGroupNew).add(curChildItem,tmpProduct);
                    }
                }
                tovarName.setText("");
                tovarPrice.setText("");
                tovarWeight.setText("");
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

        this.ELV.setAdapter(adapter);
    }


    /**
     *  Check Available for Writing to external storage
     */
    private boolean isExstStorageAvailableForWriting()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /**
     *  Save Data to the File
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

                // for all categories 
                for(int i = 0; i< childNodes.size() ;i++)
                {
                    // for all Products 
                    ArrayList<Map<String , String>> listOfCategory = childNodes.get(i);

                    for(int j = 0 ; j < listOfCategory.size(); j++)
                    {
                        Map<String , String> HashMapProd = listOfCategory.get(j);
                        String ProductName  = HashMapProd.get(ELV_CHILD_KEY_NAME);
                        String ProductPrice = HashMapProd.get(ELV_CHILD_KEY_PRICE);
                        String ProductWeigt = HashMapProd.get(ELV_CHILD_KEY_WEIGHT);

                        Tovar t = new Tovar(ProductName,Double.parseDouble(ProductPrice),Integer.parseInt(ProductWeigt),i);
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
            /**
             * Action Adding a new Item
             */
            case R.id.action_add :

                this.biulder.setView(this.dialogViewAddUpd);
                this.biulder.setTitle("Add Tovar");
                this.spinnerCategory.setAdapter(this.adapterCategory);
                isUpdate = false;
                AlertDialog dialog1 = biulder.create();
                dialog1.show();

                return true;

            /**
             * Action Update current Item
             */
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
                    this.tovarName.setText(n);
                    // -- set Old Price to EditText Field -
                    this.tovarPrice.setText(p);
                    // -- set Old Weight to EditText Field -
                    this.tovarWeight.setText(w);
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

            /**
             * Action Delete the current Item
             */
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
         allTovars.add(new Tovar("Snickers",    4.75,   45,    Tovar.CATEGORY_CHOCO));
         allTovars.add(new Tovar("Mars",        5.15,   50,    Tovar.CATEGORY_CHOCO));
         allTovars.add(new Tovar("CocaCola",    9.90, 1000,    Tovar.CATEGORY_BEVERAGES));
         allTovars.add(new Tovar("Apple",      18.50, 1000,    Tovar.CATEGORY_FRUITS));
         allTovars.add(new Tovar("Orange",     45.00, 1000,    Tovar.CATEGORY_FRUITS));
         allTovars.add(new Tovar("Bounty",      8.75,   80,    Tovar.CATEGORY_CHOCO));
         allTovars.add(new Tovar("Fanta",      11.30,  500,    Tovar.CATEGORY_BEVERAGES));
         allTovars.add(new Tovar("Beer",       14.30,  500,    Tovar.CATEGORY_BEVERAGES));
    }
}
