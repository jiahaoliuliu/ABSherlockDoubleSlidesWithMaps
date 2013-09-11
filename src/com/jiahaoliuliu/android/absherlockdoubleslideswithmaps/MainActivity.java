package com.jiahaoliuliu.android.absherlockdoubleslideswithmaps;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.app.Activity;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import android.support.v4.view.GravityCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;

public class MainActivity extends SherlockFragmentActivity {

	// Variables
	private static final int MENU_ITEM_RIGHT_LIST_ID = 10000;
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListLeft;
	private ListView mDrawerListRight;
	private ActionBarDrawerToggle mDrawerToggle;
	private MenuListAdapter mMenuAdapter;
	private String[] title;
	private String[] subtitle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	
	private GoogleMap googleMap;
	private Marker marker;
	
	private static final LatLng MADRID = new LatLng(40.417325, -3.683081);
	private static final LatLng LONDON = new LatLng(51.511214, -0.119824);
	private static final LatLng STOCKHOLM = new LatLng(59.32893, 18.06491);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawer_main);

		// Get the title
		mTitle = mDrawerTitle = getTitle();
		
		// Get the map
		googleMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		// Generate content
		title = new String[] {"Madrid", "London", "Stockholm"};
		subtitle = new String[] {"Spain", "United Kindom", "Sweden"};
		
		// Link the content
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		
		mDrawerListLeft = (ListView)findViewById(R.id.listview_drawer_left);
		mDrawerListRight = (ListView)findViewById(R.id.listview_drawer_right);
		
		// Set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		mMenuAdapter = new MenuListAdapter(MainActivity.this, title, subtitle);
		
		mDrawerListLeft.setAdapter(mMenuAdapter);
		mDrawerListRight.setAdapter(mMenuAdapter);
		
		mDrawerListLeft.setOnItemClickListener(new DrawerItemClickListener());
		mDrawerListRight.setOnItemClickListener(new DrawerItemClickListener());

		// Enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// ActionBarDrawerToggle ties together the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				R.drawable.ic_drawer,
				R.string.drawer_open,
				R.string.drawer_close) {
			
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}
			
			public void onDrawerOpened(View drawerView) {
				// Set the title on the action when drawer open
				getSupportActionBar().setTitle(mDrawerTitle);
				super.onDrawerOpened(drawerView);
			}
		};
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		if (savedInstanceState == null) {
			selectItem(0);
		}
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ITEM_RIGHT_LIST_ID, Menu
        		.NONE, "R.List")
        	.setIcon(R.drawable.ic_drawer)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(mDrawerListLeft)) {
				mDrawerLayout.closeDrawer(mDrawerListLeft);
			} else {
				if (mDrawerLayout.isDrawerOpen(mDrawerListRight)) {
					mDrawerLayout.closeDrawer(mDrawerListRight);
				}
				mDrawerLayout.openDrawer(mDrawerListLeft);
			}
		} else if (item.getItemId() == MENU_ITEM_RIGHT_LIST_ID) {
			if (mDrawerLayout.isDrawerOpen(mDrawerListRight)) {
				mDrawerLayout.closeDrawer(mDrawerListRight);
			} else {
				if (mDrawerLayout.isDrawerOpen(mDrawerListLeft)) {
					mDrawerLayout.closeDrawer(mDrawerListLeft);
				}
				mDrawerLayout.openDrawer(mDrawerListRight);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}
	
	private void selectItem(int position) {
		if (marker != null) {
			marker.remove();
		}
		switch(position) {
		case 0:
			marker = googleMap.addMarker(
					new MarkerOptions()
						.position(MADRID)
						.title("Madrid")
					);
		    // Move the camera instantly to Madrid with a zoom of 15.
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MADRID, 15));
			break;
		case 1:
			marker = googleMap.addMarker(
					
					new MarkerOptions()
						.position(LONDON)
						.title("London")
					);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LONDON, 15));
			break;
		case 2:
			marker = googleMap.addMarker(
					new MarkerOptions()
						.position(STOCKHOLM)
						.title("Stockholm")
					);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(STOCKHOLM, 15));
			break;
		}

	    // Zoom in, animating the camera.
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

		mDrawerListLeft.setItemChecked(position, true);
		
		// Get the title followed by the position
		setTitle(title[position]);
		
		// Close drawer
		if (mDrawerLayout.isDrawerOpen(mDrawerListLeft)) {
			mDrawerLayout.closeDrawer(mDrawerListLeft);
		}

		if (mDrawerLayout.isDrawerOpen(mDrawerListRight)) {
			mDrawerLayout.closeDrawer(mDrawerListRight);
		}
	}

	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

}
