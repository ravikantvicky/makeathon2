package com.stg.makeathon.agrohelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stg.makeathon.agrohelper.config.AppConstants;
import com.stg.makeathon.agrohelper.config.AppData;
import com.stg.makeathon.agrohelper.domain.CheckupData;
import com.stg.makeathon.agrohelper.domain.Disease;

import java.text.ParseException;

public class DetailsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View mView, detContainer, diseaseDetails;
    private ImageView mainImage;
    private TextView type, date, disease, remedy, cause, season, disDesc;

    public DetailsFragment() {
    }

    public static DetailsFragment newInstance() {
        DetailsFragment fragment = new DetailsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_details, container, false);
        initView();
        return mView;
    }

    private void initView() {
        mainImage = mView.findViewById(R.id.mainImage);
        type = mView.findViewById(R.id.type);
        date = mView.findViewById(R.id.date);
        disease = mView.findViewById(R.id.disease);
        remedy = mView.findViewById(R.id.remedy);
        cause = mView.findViewById(R.id.cause);
        disDesc = mView.findViewById(R.id.diseaseDesc);
        season = mView.findViewById(R.id.season);
        diseaseDetails = mView.findViewById(R.id.diseaseDetails);
        detContainer = mView.findViewById(R.id.detContainer);

        CheckupData selRecord = AppData.getInstance().getSelectedRecord();
        if (selRecord == null) {
            showErrorDialog("Error", "Unexpected Error Occurred.");
            detContainer.setVisibility(View.GONE);
            return;
        }

        Picasso.get().load(selRecord.getImageUri()).fit().placeholder(R.drawable.placeholder_img).into(mainImage);
        type.setText(selRecord.getObjType());
        try {
            date.setText(AppConstants.UI_DATE_FORMAT.format(AppConstants.DB_DATE_FORMAT.parse(selRecord.getUpdateTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        disease.setText(selRecord.getDisease());
        if (selRecord.getDisease().equalsIgnoreCase(AppConstants.KEYWORD_HEALTHY)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                disease.setTextColor(getResources().getColor(R.color.matGreen, null));
            }
        }
        diseaseDetails.setVisibility(View.GONE);
        if (selRecord.getDiseaseId() > 0
                && AppData.getInstance().getAllDisease().containsKey(selRecord.getDiseaseId())
                && !selRecord.getDisease().equalsIgnoreCase(AppConstants.KEYWORD_HEALTHY)) {
            Disease disease = AppData.getInstance().getAllDisease().get(selRecord.getDiseaseId());
            if (disease != null) {
                diseaseDetails.setVisibility(View.VISIBLE);
                disDesc.setText(disease.getDescription());
                cause.setText(disease.getCause());
                season.setText(disease.getSeasons());
                remedy.setText(disease.getTreatment());
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(getContext());
        errorDialogBuilder.setTitle(title);
        errorDialogBuilder.setMessage(message);
        errorDialogBuilder.setCancelable(true);
        errorDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        errorDialogBuilder.create().show();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDetailsFragmentAction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onDetailsFragmentAction(Uri uri);
    }
}
