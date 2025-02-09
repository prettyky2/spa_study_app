package com.example.myapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


public class DailyStudyMain extends AppApplication implements View.OnClickListener{

    private static final String TAG = "DailyStudyPage";
    private RecyclerView recyclerView;
    private AppMenuAdapter menuAdapter;
    private List<AppMenuItem> currentMenuItems;
    private Map<String, List<String>> studyDataMap = new HashMap<>();
    String studyTopic = null;

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

        initializeExcelFile();
        loadExcelData(); // 엑셀 데이터 로드
    } //initializeClass();

    private void initializeExcelFile() {
        try {
            Log.d(TAG, "Initializing Excel file...");

            File file = new File(getExternalFilesDir(null), "daily_study.xlsx");

            if (file.exists()) {
                Log.d(TAG, "Existing file found. Deleting...");
                boolean deleted = file.delete();
                if (deleted) {
                    Log.d(TAG, "Existing file deleted successfully.");
                } else {
                    Log.e(TAG, "Failed to delete existing file.");
                }
            }

            InputStream inputStream = getAssets().open("daily_study.xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            Log.d(TAG, "Excel file copied successfully. File size: " + file.length());
            Toast.makeText(this, "Excel File Initialized Successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Excel file", e);
            Toast.makeText(this, "Excel File Initialization Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    } //initializeExcelFile()

    private void loadExcelData() {
        try {
            Log.d(TAG, "loadExcelData() started"); // 실행 여부 확인

            File file = new File(getExternalFilesDir(null), "daily_study.xlsx");
            if (!file.exists()) {
                Log.e(TAG, "Excel file not found!");
                Toast.makeText(this, "Excel file not found!", Toast.LENGTH_SHORT).show();
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(0);

            studyDataMap.clear(); // 기존 데이터 초기화
            Log.d(TAG, "Cleared studyDataMap, now loading...");

            for (Row row : sheet) {
                if (row.getCell(3) == null) continue; // 4열(키 값)이 없으면 스킵

                String key = row.getCell(3).getStringCellValue().trim(); // 4열의 값
                String koreanText = row.getCell(4).getStringCellValue().trim(); // 1열 (한글 문장)
                String englishText = row.getCell(5).getStringCellValue().trim(); // 2열 (영어 문장)

                Log.d(TAG, "Processing row: Key=" + key + ", Korean=" + koreanText + ", English=" + englishText);

                if (!studyDataMap.containsKey(key)) {
                    studyDataMap.put(key, new ArrayList<>()); // 새로운 키 생성
                }

                studyDataMap.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(koreanText + " | " + englishText); // 문장 추가
            }

            workbook.close();
            fis.close();

            Log.d(TAG, "Excel Data Loaded Successfully, studyDataMap size=" + studyDataMap.size());
            Toast.makeText(this, "Excel Data Loaded Successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error loading Excel file", e);
            Toast.makeText(this, "Error loading Excel file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    } //loadExcelData()

    private void onMenuItemClicked(AppMenuItem item) {
        Intent intent = new Intent(this, DailyStudyPractice.class);
        studyTopic = getStudyTopicById(item.getId());
        List<AppMenuItem> newMenuItems = getNewMenuItemsById(item.getId());

        if (newMenuItems != null) {
            // 메뉴 아이템 변경이 필요한 경우 → RecyclerView 업데이트
            menuAdapter.updateData(newMenuItems);
        } else if (studyTopic != null) {
            // 특정 `studyTopic`이 있는 경우만 `Intent` 실행
            showStudyTestDialog(studyTopic);
        } else {
            Log.e(TAG, "No valid action found for itemId: " + item.getId());
        }
    }
    private void showStudyTestDialog(String studyTopic) {
        // 다이얼로그 생성
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_app_dialog);

        // 다이얼로그 닫기 불가능하도록 설정 (선택 사항)
        dialog.setCancelable(true);
        dialog.setOnCancelListener(dialogInterface -> dialog.dismiss());

        // 다이얼로그 뷰 초기화
        Button btnWord = dialog.findViewById(R.id.btn_word);
        Button btnStudy = dialog.findViewById(R.id.btn_study);
        Button btnTest = dialog.findViewById(R.id.btn_test);

        // 버튼 이벤트 설정
        btnWord.setOnClickListener(v -> {
            Intent intent = new Intent(this, DailyWordPractice.class);
            intent.putExtra("study_topic", studyTopic);
            startActivity(intent);
            dialog.dismiss();
        });

        btnStudy.setOnClickListener(v -> {
            navigateToStudyPractice(studyTopic, 0); // 0: STUDY 모드
            dialog.dismiss();
        });

        btnTest.setOnClickListener(v -> {
            navigateToStudyPractice(studyTopic, 1); // 1: TEST 모드
            dialog.dismiss();
        });

        // 다이얼로그 표시
        dialog.show();
    }

    private void navigateToStudyPractice(String studyTopic, int mode) {
        Intent intent = new Intent(this, DailyStudyPractice.class);
        intent.putExtra("study_topic", studyTopic);
        intent.putExtra("image_popup", getImagePopupByTopic(studyTopic));
        intent.putExtra("mode", mode); // 0: STUDY, 1: TEST

        List<String> sentences = studyDataMap.get(studyTopic);
        if (sentences != null && !sentences.isEmpty()) {
            intent.putStringArrayListExtra("sentences", new ArrayList<>(sentences));
        } else {
            Log.e(TAG, "No data found for topic: " + studyTopic);
            intent.putStringArrayListExtra("sentences", new ArrayList<>()); // 빈 리스트 추가
        }

        startActivity(intent);
    }

    private String getStudyTopicById(int id) {
        switch (id) {
            case 1101: return "personal_question_daily_routine_company";
            case 1102: return "personal_question_daily_routine_ride";
            case 1103: return "personal_question_daily_routine_travel";
            case 1201: return "personal_question_things_i_like_movie";
            case 1202: return "personal_question_things_i_like_food";
            case 1203: return "personal_question_things_i_like_place";
            case 1301: return "personal_question_things_i_do_health";
            case 1302: return "personal_question_things_i_do_stress";
            case 1303: return "personal_question_things_i_do_relationship";
            case 1401: return "personal_question_past_work_weather";
            case 1402: return "personal_question_past_work_travel";
            case 1403: return "personal_question_past_work_weekend";
            case 1501: return "personal_question_guessing_society";
            case 1502: return "personal_question_guessing_vacation";
            case 1503: return "personal_question_guessing_policy";
            case 2001: return "summarize_passage_common_common";
            case 2002: return "summarize_passage_daily_life_1_1";
            case 2003: return "summarize_passage_daily_life_2_2";
            case 2004: return "summarize_passage_company_company";
            case 2005: return "summarize_passage_technology_technology";
            case 2006: return "summarize_passage_daily_life_3_3";
            case 2007: return "summarize_passage_effect_of_music_effect_of_music";
            case 2008: return "summarize_passage_importance_helmet_importance_helmet";
            case 2009: return "summarize_passage_internet_sns_internet_sns";
            case 2010: return "summarize_passage_importance_sleep_importance_sleep";
            case 2011: return "summarize_passage_internet_internet";
            case 3101: return "tell_your_opinion_company_clothes";
            case 3102: return "tell_your_opinion_company_relationship";
            case 3103: return "tell_your_opinion_company_motivation";
            case 3201: return "tell_your_opinion_technology_internet_sns";
            case 3202: return "tell_your_opinion_technology_changeway";
            case 3203: return "tell_your_opinion_technology_newtech";
            case 3301: return "tell_your_opinion_societyculture_manners";
            case 3302: return "tell_your_opinion_societyculture_sexchange";
            case 3303: return "tell_your_opinion_societyculture_moving";
            case 3401: return "tell_your_opinion_raw_policy_smokingarea";
            case 3402: return "tell_your_opinion_raw_policy_tech_develop";
            case 3403: return "tell_your_opinion_raw_policy_update_law";
            case 3501: return "tell_your_opinion_prefer_how_to_feedback";
            case 3502: return "tell_your_opinion_prefer_how_to_pay";
            case 3503: return "tell_your_opinion_prefer_way_to_life";
            case 3601: return "tell_your_opinion_agree_or_not_aboard";
            case 3602: return "tell_your_opinion_agree_or_not_work_at_home";
            case 3603: return "tell_your_opinion_agree_or_not_way_to_study";
            case 4101: return "describe_graph_photo_common_bar_common";
            case 4102: return "describe_graph_photo_bar_1_bar_1";
            case 4103: return "describe_graph_photo_bar_2_bar_2";
            case 4104: return "describe_graph_photo_bar_3_bar_3";
            case 4201: return "describe_graph_photo_common_pie_common";
            case 4202: return "describe_graph_photo_pie_4_pie_1";
            case 4203: return "describe_graph_photo_pie_5_pie_2";
            case 4204: return "describe_graph_photo_pie_6_pie_3";
            case 4301: return "describe_graph_photo_common_line_common";
            case 4302: return "describe_graph_photo_line_7_line_1";
            case 4303: return "describe_graph_photo_line_8_line_2";
            case 4304: return "describe_graph_photo_line_9_line_3";
            case 4401: return "describe_graph_photo_common_describe_photo_common";
            case 4402: return "describe_graph_photo_describe_photo_10_describe_photo_1";
            case 4403: return "describe_graph_photo_describe_photo_11_describe_photo_2";
            case 4404: return "describe_graph_photo_describe_photo_12_describe_photo_3";
            case 4501: return "describe_graph_photo_common_compare_photo_common";
            case 4502: return "describe_graph_photo_compare_photo_13_compare_photo_1";
            case 4503: return "describe_graph_photo_compare_photo_14_compare_photo_2";
            case 4504: return "describe_graph_photo_compare_photo_15_compare_photo_3";
            case 4601: return "describe_graph_photo_common_prefer_photo_common";
            case 4602: return "describe_graph_photo_prefer_photo_16_prefer_photo_1";
            case 4603: return "describe_graph_photo_prefer_photo_17_prefer_photo_2";
            case 4604: return "describe_graph_photo_prefer_photo_18_prefer_photo_3";
            case 4701: return "describe_graph_photo_common_sell_object_common";
            case 4702: return "describe_graph_photo_sell_object_19_sell_object_1";
            case 4703: return "describe_graph_photo_sell_object_20_sell_object_2";
            case 4704: return "describe_graph_photo_sell_object_21_sell_object_3";
            default: return null;
        }
    }

    private List<AppMenuItem> getNewMenuItemsById(int id) {
        switch (id) {
            case 1000: return dailyStudyPagePersonalQuestionItem();
            case 1100: return dailyStudyPagePersonalQuestionDailyRoutineItem();
            case 1200: return dailyStudyPagePersonalQuestionThingsILikeItem();
            case 1300: return dailyStudyPagePersonalQuestionThingsIDoItem();
            case 1400: return dailyStudyPagePersonalQuestionPastWorkItem();
            case 1500: return dailyStudyPagePersonalQuestionGuessingItem();
            case 2000: return dailyStudyPageSummarizePassageItem();
            case 3000: return dailyStudyPageTellYourOpinionItem();
            case 3100: return dailyStudyPageTellYourOpinionCompanyItem();
            case 3200: return dailyStudyPageTellYourOpinionTechnologyItem();
            case 3300: return dailyStudyPageTellYourOpinionSocietyCultureItem();
            case 3400: return dailyStudyPageTellYourOpinionRawPolicyItem();
            case 3500: return dailyStudyPageTellYourOpinionPreferItem();
            case 3600: return dailyStudyPageTellYourOpinionAgreeOrNotItem();
            case 4000: return dailyStudyPageDescribeGraphAndPhotoItem();
            case 4100: return dailyStudyPageDescribeGraphAndPhotoBarGraphItem();
            case 4200: return dailyStudyPageDescribeGraphAndPhotoPieGraphItem();
            case 4300: return dailyStudyPageDescribeGraphAndPhotoLineGraphItem();
            case 4400: return dailyStudyPageDescribeGraphAndPhotoDescribePhotoItem();
            case 4500: return dailyStudyPageDescribeGraphAndPhotoComparePhotoItem();
            case 4600: return dailyStudyPageDescribeGraphAndPhotoPreferPhotoItem();
            case 4700: return dailyStudyPageDescribeGraphAndPhotoSellObjectItem();
            default: return null;
        }
    }

    private String getImagePopupByTopic(String studyTopic) {
        switch (studyTopic) {
            case "describe_graph_photo_bar_1_bar_1": return "graph_1_bar_1";
            case "describe_graph_photo_bar_2_bar_2": return "graph_2_bar_2";
            case "describe_graph_photo_bar_3_bar_3": return "graph_3_bar_3";
            case "describe_graph_photo_pie_4_pie_1": return "graph_4_pie_1";
            case "describe_graph_photo_pie_5_pie_2": return "graph_5_pie_2";
            case "describe_graph_photo_pie_6_pie_3": return "graph_6_pie_3";
            case "describe_graph_photo_line_7_line_1": return "graph_7_line_1";
            case "describe_graph_photo_line_8_line_2": return "graph_8_line_2";
            case "describe_graph_photo_line_9_line_3": return "graph_9_line_3";
            case "describe_graph_photo_describe_photo_10_describe_photo_1": return "photo_1_describe_1";
            case "describe_graph_photo_describe_photo_11_describe_photo_2": return "photo_2_describe_2";
            case "describe_graph_photo_describe_photo_12_describe_photo_3": return "photo_3_describe_3";
            case "describe_graph_photo_compare_photo_13_compare_photo_1": return "photo_4_compare_1";
            case "describe_graph_photo_compare_photo_14_compare_photo_2": return "photo_5_compare_2";
            case "describe_graph_photo_compare_photo_15_compare_photo_3": return "photo_6_compare_3";
            case "describe_graph_photo_prefer_photo_16_prefer_photo_1": return "photo_7_prefer_1";
            case "describe_graph_photo_prefer_photo_17_prefer_photo_2": return "photo_8_prefer_2";
            case "describe_graph_photo_prefer_photo_18_prefer_photo_3" : return "photo_9_prefer_3";
            case "describe_graph_photo_sell_object_19_sell_object_1": return "photo_10_sell_1";
            case "describe_graph_photo_sell_object_20_sell_object_2": return "photo_11_sell_2";
            case "describe_graph_photo_sell_object_21_sell_object_3": return "photo_12_sell_3";
            default: return "no";
        }
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
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_agree_or_not_aboard), 3601));      // 3-6-01
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_agree_or_not_work_at_home), 3602));// 3-6-02
        menu.add(new AppMenuItem(getString(R.string.tell_your_opinion_agree_or_not_way_to_study), 3603));// 3-6-03
        return menu;
    }
    private List<AppMenuItem> dailyStudyPageDescribeGraphAndPhotoItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_bar_graph), 4100));        // 4-1
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_pie_graph), 4200));        // 4-2
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_line_graph), 4300));       // 4-3
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_photo_describe), 4400));   // 4-4
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_photo_compare), 4500));    // 4-5
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_photo_prefer), 4600));     // 4-6
        menu.add(new AppMenuItem(getString(R.string.describe_graph_and_photo_sell_object), 4700));      // 4-7
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageDescribeGraphAndPhotoBarGraphItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_bar_graph_common), 4101));      // 4-1-01
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_bar_graph_1_bar_1), 4102));     // 4-1-02
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_bar_graph_2_bar_2), 4103));     // 4-1-03
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_bar_graph_3_bar_3), 4104));     // 4-1-04
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageDescribeGraphAndPhotoPieGraphItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_pie_graph_common), 4201));      // 4-2-01
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_pie_graph_4_pie_1), 4202));     // 4-2-02
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_pie_graph_5_pie_2), 4203));     // 4-2-03
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_pie_graph_6_pie_3), 4204));     // 4-2-04
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageDescribeGraphAndPhotoLineGraphItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_line_graph_common), 4301));     // 4-3-01
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_line_graph_7_line_1), 4302));   // 4-3-02
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_line_graph_8_line_2), 4303));   // 4-3-03
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_line_graph_9_line_3), 4304));   // 4-3-04
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageDescribeGraphAndPhotoDescribePhotoItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_describe_photo_common), 4401)); // 4-4-01
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_10_describe_photo_1), 4402));   // 4-4-02
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_11_describe_photo_2), 4403));   // 4-4-03
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_12_describe_photo_3), 4404));   // 4-4-04
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageDescribeGraphAndPhotoComparePhotoItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_compare_photo_common), 4501)); // 4-5-01
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_13_compare_photo_1), 4502));   // 4-5-02
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_14_compare_photo_2), 4503));   // 4-5-03
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_15_compare_photo_3), 4504));   // 4-5-04
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageDescribeGraphAndPhotoPreferPhotoItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_prefer_photo_common), 4601)); // 4-6-01
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_16_prefer_photo_1), 4602));   // 4-6-02
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_17_prefer_photo_2), 4603));   // 4-6-03
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_18_prefer_photo_3), 4604));   // 4-6-04
        return menu;
    }

    private List<AppMenuItem> dailyStudyPageDescribeGraphAndPhotoSellObjectItem() {
        List<AppMenuItem> menu = new ArrayList<>();
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_sell_object_common), 4701)); // 4-7-01
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_19_sell_object_1), 4702));   // 4-7-02
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_20_sell_object_2), 4703));   // 4-7-03
        menu.add(new AppMenuItem(getString(R.string.describe_graph_photo_21_sell_object_3), 4704));   // 4-7-04
        return menu;
    }

}
