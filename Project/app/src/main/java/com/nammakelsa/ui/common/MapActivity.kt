package com.nammakelsa.ui.common

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.repository.WorkerRepository
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.content.Intent
import com.nammakelsa.ui.customer.WorkerDetailsActivity

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var repository: WorkerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load/Initialize the osmdroid configuration
        Configuration.getInstance().userAgentValue = packageName
        
        setContentView(R.layout.activity_map)

        val database = AppDatabase.getDatabase(this)
        repository = WorkerRepository(
            database.workerDao(),
            database.bookingDao(),
            database.reviewDao(),
            database.favoriteDao()
        )

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(12.0)
        val startPoint = GeoPoint(12.9716, 77.5946) // Bangalore
        mapController.setCenter(startPoint)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadWorkerMarkers()
    }

    private fun loadWorkerMarkers() {
        repository.getAllAvailableWorkersRealtime { workers ->
            mapView.overlays.clear()
            
            if (workers.isEmpty()) {
                Toast.makeText(this, "No available workers found", Toast.LENGTH_SHORT).show()
            }

            for (worker in workers) {
                if (worker.latitude != 0.0 && worker.longitude != 0.0) {
                    val nodeMarker = Marker(mapView)
                    nodeMarker.position = GeoPoint(worker.latitude, worker.longitude)
                    nodeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    nodeMarker.title = worker.name
                    nodeMarker.snippet = "${worker.skill} • ₹${worker.dailyRate}/day"
                    nodeMarker.setOnMarkerClickListener { marker, mapView ->
                        val intent = Intent(this@MapActivity, WorkerDetailsActivity::class.java)
                        intent.putExtra("WORKER_ID", worker.uid)
                        startActivity(intent)
                        true
                    }
                    mapView.overlays.add(nodeMarker)
                }
            }
            mapView.invalidate() // Refresh the map
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
