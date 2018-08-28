package com.stg.makeathon.agrohelper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stg.makeathon.agrohelper.CheckupDataListFragment.OnListFragmentInteractionListener;
import com.stg.makeathon.agrohelper.domain.CheckupData;

import java.util.List;

public class CheckupDataRecyclerViewAdapter extends RecyclerView.Adapter<CheckupDataRecyclerViewAdapter.ViewHolder> {

    private final List<CheckupData> mValues;
    private final OnListFragmentInteractionListener mListener;

    public CheckupDataRecyclerViewAdapter(List<CheckupData> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_checkupdata, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        Picasso.get().load(mValues.get(position).getImageUri()).resizeDimen(R.dimen.list_img_width, R.dimen.list_img_height).into(holder.checkImg);
        holder.objType.setText(mValues.get(position).getObjType());
        holder.disease.setText(mValues.get(position).getDisease());
        holder.infArea.setText(mValues.get(position).getInfectedArea());
        holder.remedy.setText(mValues.get(position).getRemedy());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onCheckupHistorySelection(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView checkImg;
        public final TextView objType, disease, infArea, remedy;
        public CheckupData mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            checkImg = view.findViewById(R.id.checkImg);
            objType = view.findViewById(R.id.objType);
            disease = view.findViewById(R.id.disease);
            infArea = view.findViewById(R.id.infectedArr);
            remedy = view.findViewById(R.id.remedy);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + objType.getText() + "'";
        }
    }
}
