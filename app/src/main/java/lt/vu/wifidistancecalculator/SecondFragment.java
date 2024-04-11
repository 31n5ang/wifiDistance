package lt.vu.wifidistancecalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lt.vu.wifidistancecalculator.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {
    private FragmentSecondBinding binding;
    private List<String> scanResults = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewScanResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData(); // 데이터 로드

        ScanResultsAdapter adapter = new ScanResultsAdapter(getContext(), scanResults);

        // 클릭 리스너 설정
        adapter.setClickListener(position -> {
            // 여기서 position은 삭제하려는 아이템의 위치입니다.
            scanResults.remove(position); // 리스트에서 해당 위치의 아이템을 제거
            adapter.notifyItemRemoved(position); // 어댑터에 아이템 제거를 알림
            updateDataFile(); // 변경된 데이터로 파일 업데이트
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        File file = new File(getContext().getFilesDir(), "data.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                scanResults.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDataFile() {
        File file = new File(getContext().getFilesDir(), "data.txt");
        try (FileWriter writer = new FileWriter(file, false)) {
            for (String result : scanResults) {
                writer.write(result + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
