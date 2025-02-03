package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuAdapter;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DailyStudyPage extends AppCompatActivity implements View.OnClickListener{

    private RecyclerView recyclerView;
    private AppMenuAdapter menuAdapter;
    private List<AppMenuItem> currentMenuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_study_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.daily_study_page_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeClass();

    } //onCreate();


    @Override
    public void onClick(View v) {
    } //onClick();


    private void initializeClass() {
        //initialize object

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recycler_view_menu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentMenuItems = dailyStudyPageMainMenuItem();
        menuAdapter = new AppMenuAdapter(this, currentMenuItems, this::onMenuItemClicked);
        recyclerView.setAdapter(menuAdapter);






    } //initializeClass();

    // 아이템 클릭 시 호출되는 메서드

    private void onMenuItemClicked(AppMenuItem item) {
        List<AppMenuItem> newMenuItems;

        switch (item.getId()) {
            case 1:
                //newMenuItems = getPersonalQuestions();
                break;
            case 2:
                //newMenuItems = getSummarizePassages();
                break;
            case 3:
                //newMenuItems = getTellYourOpinions();
                break;
            case 4:
                //newMenuItems = getDescribeGraphs();
                break;
            default:
                //newMenuItems = getMainMenu(); // 기본으로 돌아가는 기능 추가
                break;
        }

        // 데이터 변경 후 UI 갱신
        //menuAdapter.updateData(newMenuItems);
    }



//-------------------Item List---------------------------------------------------------
    private List<AppMenuItem> dailyStudyPageMainMenuItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.daily_study_personal_question), 1000));             // 1
        menu.add(new AppMenuItem(getString(R.string.daily_study_summarize_passage), 2000));             // 2
        menu.add(new AppMenuItem(getString(R.string.daily_study_tell_your_opinion), 3000));             // 3
        menu.add(new AppMenuItem(getString(R.string.daily_study_describe_graph_and_photo), 4000));      // 4
        return menu;
    }

    private List<AppMenuItem> dailyStudyPagePersonalQuestionItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.personal_question_daily_routine), 1100));            // 1-1
        menu.add(new AppMenuItem(getString(R.string.personal_question_things_i_like), 1200));            // 1-2
        menu.add(new AppMenuItem(getString(R.string.personal_question_things_i_do), 1300));              // 1-3
        menu.add(new AppMenuItem(getString(R.string.personal_question_past_work), 1400));                // 1-4
        menu.add(new AppMenuItem(getString(R.string.personal_question_guessing), 1500));                 // 1-5
        return menu;
    }

    private List<AppMenuItem> dailyStudyPagePersonalQuestionDailyRoutineItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.personal_question_daily_routine_company), 1101));    // 1-1-01
        menu.add(new AppMenuItem(getString(R.string.personal_question_daily_routine_ride), 1102));       // 1-1-02
        menu.add(new AppMenuItem(getString(R.string.personal_question_daily_routine_travel), 1103));     // 1-1-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPagePersonalQuestionThingsILikeItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.personal_question_things_i_like_movie), 1201));     // 1-2-01
        menu.add(new AppMenuItem(getString(R.string.personal_question_things_i_like_food), 1202));      // 1-2-02
        menu.add(new AppMenuItem(getString(R.string.personal_question_things_i_like_Place), 1203));     // 1-2-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPagePersonalQuestionThingsIDoItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.personal_question_things_i_do_health), 1301));    // 1-3-01
        menu.add(new AppMenuItem(getString(R.string.personal_question_things_i_do_stress), 1302));    // 1-3-02
        menu.add(new AppMenuItem(getString(R.string.personal_question_things_i_do_relationship), 1303));// 1-3-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPagePersonalQuestionPastWorkItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.personal_question_past_work_weather), 1401));       // 1-4-01
        menu.add(new AppMenuItem(getString(R.string.personal_question_past_work_travel), 1402));        // 1-4-02
        menu.add(new AppMenuItem(getString(R.string.personal_question_past_work_weekend), 1403));       // 1-4-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPagePersonalQuestionGuessingItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.personal_question_guessing_society), 1501));        // 1-5-01
        menu.add(new AppMenuItem(getString(R.string.personal_question_guessing_vacation), 1502));       // 1-5-02
        menu.add(new AppMenuItem(getString(R.string.personal_question_guessing_policy), 1503));         // 1-5-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageSummarizePassageItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_common), 2001));                  // 2-001
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_daily_life_1), 2002));            // 2-002
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_daily_life_2), 2003));            // 2-003
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_company), 2004));                 // 2-004
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_technology), 2005));              // 2-005
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_daily_life_3), 2006));            // 2-006
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_effect_of_music), 2007));         // 2-007
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_importance_helmet), 2008));       // 2-008
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_internet_sns), 2009));            // 2-009
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_importance_sleep), 2010));        // 2-010
        menu.add(new AppMenuItem(getString(R.string.summarize_passage_internet), 2011));                // 2-011
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageTellYourOpinionItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_company), 3100));                 // 3-1
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_technology), 3200));              // 3-2
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_societyculture), 3300));          // 3-3
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_raw_policy), 3400));              // 3-4
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_prefer), 3500));                  // 3-5
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_agree_or_not), 3600));            // 3-6
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageTellYourOpinionCompanyItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_company_clothes), 3101));         // 3-1-01
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_company_relationship), 3102));    // 3-1-02
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_company_motivation), 3103));      // 3-1-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageTellYourOpinionTechnologyItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_technology_internet_sns), 3201)); // 3-2-01
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_technology_changeway), 3202));    // 3-2-02
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_technology_newtech), 3203));      // 3-2-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageTellYourOpinionSocietyCultureItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_societyculture_manners), 3301));  // 3-3-01
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_societyculture_sexchange), 3302));// 3-3-02
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_societyculture_moving), 3303));   // 3-3-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageTellYourOpinionRawPolicyItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_raw_policy_smokingarea), 3401));  // 3-4-01
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_raw_policy_tech_develop), 3402)); // 3-4-02
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_raw_policy_update_law), 3403));   // 3-4-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageTellYourOpinionPreferItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_prefer_how_to_feedback), 3501));  // 3-5-01
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_prefer_how_to_pay), 3502));       // 3-5-02
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_prefer_way_to_life), 3503));      // 3-5-03
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageTellYourOpinionAgreeOrNotItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_agree_or_not_aboard), 3601));     // 3-6-01
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_agree_or_not_work_at_home), 3602));// 3-6-02
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_agree_or_not_way_to_study), 3603));// 3-6-03
        return menu;
    }
    private List<AppMenuItem> dailyStudyPageDescribeGraphAndPhotoItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_bar_graph), 1));
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_pie_graph), 2));
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_line_graph), 3));
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_photo), 4));
        return menu;
    }





}
