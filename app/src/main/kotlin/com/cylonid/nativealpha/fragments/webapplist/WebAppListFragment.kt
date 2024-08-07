package com.cylonid.nativealpha.fragments.webapplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.DataManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView

class WebAppListFragment : Fragment(R.layout.fragment_web_app_list) {
    private lateinit var adapter: WebAppListAdapter

    private lateinit var list: DragDropSwipeRecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WebAppListAdapter(DataManager.getInstance().activeWebsites, requiredActivity())

        list = view.findViewById(R.id.web_app_list)
        list.layoutManager = LinearLayoutManager(requiredActivity())
        list.adapter = adapter
        list.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING

    }

    public fun updateWebAppList() {
        adapter.updateWebAppList()
    }

    private fun requiredActivity(): FragmentActivity {
        return requireNotNull(activity) { "WebAppListFragment is not attached to an activity." }
    }
}