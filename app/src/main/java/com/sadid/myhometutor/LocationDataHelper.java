package com.sadid.myhometutor;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class LocationDataHelper {
    
    public static LinkedHashMap<String, LinkedHashMap<String, List<String>>> getLocationData() {
        LinkedHashMap<String, LinkedHashMap<String, List<String>>> locationData = new LinkedHashMap<>();
        
        // Dhaka Division
        LinkedHashMap<String, List<String>> dhakaDistricts = new LinkedHashMap<>();
        dhakaDistricts.put("Dhaka", Arrays.asList("Dhanmondi", "Gulshan", "Mirpur", "Uttara", "Mohammadpur", "Banani", "Badda", "Rampura", "Motijheel"));
        dhakaDistricts.put("Gazipur", Arrays.asList("Gazipur Sadar", "Kaliakair", "Kapasia", "Sreepur", "Kaliganj"));
        dhakaDistricts.put("Narayanganj", Arrays.asList("Narayanganj Sadar", "Bandar", "Rupganj", "Sonargaon"));
        dhakaDistricts.put("Tangail", Arrays.asList("Tangail Sadar", "Basail", "Bhuapur", "Delduar", "Ghatail", "Gopalpur", "Kalihati", "Madhupur", "Mirzapur", "Nagarpur", "Sakhipur"));
        dhakaDistricts.put("Munshiganj", Arrays.asList("Munshiganj Sadar", "Gazaria", "Lohajang", "Sirajdikhan", "Sreenagar", "Tongibari"));
        dhakaDistricts.put("Manikganj", Arrays.asList("Manikganj Sadar", "Singair", "Shibalaya", "Saturia", "Harirampur", "Ghior", "Daulatpur"));
        dhakaDistricts.put("Narsingdi", Arrays.asList("Narsingdi Sadar", "Belabo", "Monohardi", "Palash", "Raipura", "Shibpur"));
        dhakaDistricts.put("Faridpur", Arrays.asList("Faridpur Sadar", "Alfadanga", "Bhanga", "Boalmari", "Charbhadrasan", "Madhukhali", "Nagarkanda", "Sadarpur", "Saltha"));
        dhakaDistricts.put("Gopalganj", Arrays.asList("Gopalganj Sadar", "Kashiani", "Kotalipara", "Muksudpur", "Tungipara"));
        dhakaDistricts.put("Madaripur", Arrays.asList("Madaripur Sadar", "Kalkini", "Rajoir", "Shibchar"));
        dhakaDistricts.put("Rajbari", Arrays.asList("Rajbari Sadar", "Baliakandi", "Goalandaghat", "Pangsha", "Kalukhali"));
        dhakaDistricts.put("Shariatpur", Arrays.asList("Shariatpur Sadar", "Bhedarganj", "Damudya", "Gosairhat", "Naria", "Zajira"));
        dhakaDistricts.put("Kishoreganj", Arrays.asList("Kishoreganj Sadar", "Bajitpur", "Bhairab", "Hossainpur", "Itna", "Karimganj", "Katiadi", "Kuliarchar", "Mithamain", "Nikli", "Pakundia", "Tarail"));
        locationData.put("Dhaka", dhakaDistricts);
        
        // Chittagong Division
        LinkedHashMap<String, List<String>> chittagongDistricts = new LinkedHashMap<>();
        chittagongDistricts.put("Chittagong", Arrays.asList("Agrabad", "Panchlaish", "Halishahar", "Khulshi", "Pahartali", "Double Mooring", "Kotwali", "Chandgaon", "Bayazid", "Bakalia"));
        chittagongDistricts.put("Cox's Bazar", Arrays.asList("Cox's Bazar Sadar", "Chakaria", "Kutubdia", "Maheshkhali", "Ramu", "Teknaf", "Ukhia", "Pekua"));
        chittagongDistricts.put("Comilla", Arrays.asList("Comilla Sadar", "Barura", "Brahmanpara", "Burichang", "Chandina", "Chauddagram", "Daudkandi", "Debidwar", "Homna", "Laksam", "Meghna", "Muradnagar", "Nangalkot", "Titas"));
        chittagongDistricts.put("Feni", Arrays.asList("Feni Sadar", "Chhagalnaiya", "Daganbhuiyan", "Parshuram", "Sonagazi", "Fulgazi"));
        chittagongDistricts.put("Khagrachhari", Arrays.asList("Khagrachhari Sadar", "Dighinala", "Lakshmichhari", "Mahalchhari", "Manikchhari", "Matiranga", "Panchhari", "Ramgarh"));
        chittagongDistricts.put("Lakshmipur", Arrays.asList("Lakshmipur Sadar", "Raipur", "Ramganj", "Ramgati", "Kamalnagar"));
        chittagongDistricts.put("Noakhali", Arrays.asList("Noakhali Sadar", "Begumganj", "Chatkhil", "Companiganj", "Hatiya", "Senbagh", "Sonaimuri", "Subarnachar", "Kabirhat"));
        chittagongDistricts.put("Rangamati", Arrays.asList("Rangamati Sadar", "Bagaichhari", "Barkal", "Belaichhari", "Juraichhari", "Kaptai", "Kawkhali", "Langadu", "Naniarchar", "Rajasthali"));
        chittagongDistricts.put("Bandarban", Arrays.asList("Bandarban Sadar", "Alikadam", "Lama", "Naikhongchhari", "Rowangchhari", "Ruma", "Thanchi"));
        chittagongDistricts.put("Brahmanbaria", Arrays.asList("Brahmanbaria Sadar", "Akhaura", "Ashuganj", "Bancharampur", "Bijoynagar", "Kasba", "Nabinagar", "Nasirnagar", "Sarail"));
        chittagongDistricts.put("Chandpur", Arrays.asList("Chandpur Sadar", "Faridganj", "Haimchar", "Hajiganj", "Kachua", "Matlab Dakshin", "Matlab Uttar", "Shahrasti"));
        locationData.put("Chittagong", chittagongDistricts);
        
        // Sylhet Division
        LinkedHashMap<String, List<String>> sylhetDistricts = new LinkedHashMap<>();
        sylhetDistricts.put("Sylhet", Arrays.asList("Sylhet Sadar", "Beanibazar", "Bishwanath", "Companigonj", "Fenchuganj", "Golapganj", "Gowainghat", "Jaintiapur", "Kanaighat", "Zakiganj", "Dakshin Surma"));
        sylhetDistricts.put("Moulvibazar", Arrays.asList("Moulvibazar Sadar", "Barlekha", "Juri", "Kamalganj", "Kulaura", "Rajnagar", "Sreemangal"));
        sylhetDistricts.put("Habiganj", Arrays.asList("Habiganj Sadar", "Ajmiriganj", "Bahubal", "Baniyachong", "Chunarughat", "Lakhai", "Madhabpur", "Nabiganj", "Shayestaganj"));
        sylhetDistricts.put("Sunamganj", Arrays.asList("Sunamganj Sadar", "Bishwamvarpur", "Chhatak", "Dakshin Sunamganj", "Derai", "Dharamapasha", "Dowarabazar", "Jagannathpur", "Jamalganj", "Salla", "Tahirpur"));
        locationData.put("Sylhet", sylhetDistricts);
        
        // Rajshahi Division
        LinkedHashMap<String, List<String>> rajshahiDistricts = new LinkedHashMap<>();
        rajshahiDistricts.put("Rajshahi", Arrays.asList("Rajshahi Sadar", "Bagha", "Bagmara", "Charghat", "Durgapur", "Godagari", "Mohanpur", "Paba", "Puthia", "Tanore"));
        rajshahiDistricts.put("Bogra", Arrays.asList("Bogra Sadar", "Adamdighi", "Dhunat", "Dhupchanchia", "Gabtali", "Kahaloo", "Nandigram", "Sariakandi", "Shajahanpur", "Sherpur", "Shibganj", "Sonatola"));
        rajshahiDistricts.put("Pabna", Arrays.asList("Pabna Sadar", "Atgharia", "Bera", "Bhangura", "Chatmohar", "Faridpur", "Ishwardi", "Santhia", "Sujanagar"));
        rajshahiDistricts.put("Natore", Arrays.asList("Natore Sadar", "Bagatipara", "Baraigram", "Gurudaspur", "Lalpur", "Naldanga", "Singra"));
        rajshahiDistricts.put("Sirajganj", Arrays.asList("Sirajganj Sadar", "Belkuchi", "Chauhali", "Kamarkhanda", "Kazipur", "Raiganj", "Shahjadpur", "Tarash", "Ullahpara"));
        rajshahiDistricts.put("Chapainawabganj", Arrays.asList("Chapainawabganj Sadar", "Bholahat", "Gomastapur", "Nachole", "Shibganj"));
        rajshahiDistricts.put("Naogaon", Arrays.asList("Naogaon Sadar", "Atrai", "Badalgachhi", "Dhamoirhat", "Manda", "Mahadebpur", "Niamatpur", "Patnitala", "Porsha", "Raninagar", "Sapahar"));
        rajshahiDistricts.put("Joypurhat", Arrays.asList("Joypurhat Sadar", "Akkelpur", "Kalai", "Khetlal", "Panchbibi"));
        locationData.put("Rajshahi", rajshahiDistricts);
        
        // Khulna Division
        LinkedHashMap<String, List<String>> khulnaDistricts = new LinkedHashMap<>();
        khulnaDistricts.put("Khulna", Arrays.asList("Khulna Sadar", "Batiaghata", "Dacope", "Dighalia", "Dumuria", "Koyra", "Paikgachha", "Phultala", "Rupsa", "Terokhada"));
        khulnaDistricts.put("Jessore", Arrays.asList("Jessore Sadar", "Abhaynagar", "Bagherpara", "Chaugachha", "Jhikargachha", "Keshabpur", "Manirampur", "Sharsha"));
        khulnaDistricts.put("Satkhira", Arrays.asList("Satkhira Sadar", "Assasuni", "Debhata", "Kalaroa", "Kaliganj", "Shyamnagar", "Tala"));
        khulnaDistricts.put("Bagerhat", Arrays.asList("Bagerhat Sadar", "Chitalmari", "Fakirhat", "Kachua", "Mollahat", "Mongla", "Morrelganj", "Rampal", "Sarankhola"));
        khulnaDistricts.put("Narail", Arrays.asList("Narail Sadar", "Kalia", "Lohagara"));
        khulnaDistricts.put("Magura", Arrays.asList("Magura Sadar", "Mohammadpur", "Shalikha", "Sreepur"));
        khulnaDistricts.put("Chuadanga", Arrays.asList("Chuadanga Sadar", "Alamdanga", "Damurhuda", "Jibannagar"));
        khulnaDistricts.put("Kushtia", Arrays.asList("Kushtia Sadar", "Bheramara", "Daulatpur", "Khoksa", "Kumarkhali", "Mirpur"));
        khulnaDistricts.put("Meherpur", Arrays.asList("Meherpur Sadar", "Gangni", "Mujibnagar"));
        khulnaDistricts.put("Jhenaidah", Arrays.asList("Jhenaidah Sadar", "Harinakunda", "Kaliganj", "Kotchandpur", "Maheshpur", "Shailkupa"));
        locationData.put("Khulna", khulnaDistricts);
        
        // Barisal Division
        LinkedHashMap<String, List<String>> barisalDistricts = new LinkedHashMap<>();
        barisalDistricts.put("Barisal", Arrays.asList("Barisal Sadar", "Agailjhara", "Babuganj", "Bakerganj", "Banaripara", "Gaurnadi", "Hizla", "Mehendiganj", "Muladi", "Wazirpur"));
        barisalDistricts.put("Patuakhali", Arrays.asList("Patuakhali Sadar", "Bauphal", "Dashmina", "Dumki", "Galachipa", "Kalapara", "Mirzaganj", "Rangabali"));
        barisalDistricts.put("Bhola", Arrays.asList("Bhola Sadar", "Burhanuddin", "Char Fasson", "Daulatkhan", "Lalmohan", "Manpura", "Tazumuddin"));
        barisalDistricts.put("Pirojpur", Arrays.asList("Pirojpur Sadar", "Bhandaria", "Kawkhali", "Mathbaria", "Nazirpur", "Nesarabad", "Zianagar"));
        barisalDistricts.put("Jhalokati", Arrays.asList("Jhalokati Sadar", "Kathalia", "Nalchity", "Rajapur"));
        barisalDistricts.put("Barguna", Arrays.asList("Barguna Sadar", "Amtali", "Bamna", "Betagi", "Patharghata", "Taltali"));
        locationData.put("Barisal", barisalDistricts);
        
        // Rangpur Division
        LinkedHashMap<String, List<String>> rangpurDistricts = new LinkedHashMap<>();
        rangpurDistricts.put("Rangpur", Arrays.asList("Rangpur Sadar", "Badarganj", "Gangachhara", "Kaunia", "Mithapukur", "Pirgachha", "Pirganj", "Taraganj"));
        rangpurDistricts.put("Dinajpur", Arrays.asList("Dinajpur Sadar", "Birampur", "Birganj", "Biral", "Bochaganj", "Chirirbandar", "Fulbari", "Ghoraghat", "Hakimpur", "Kaharole", "Khansama", "Nawabganj", "Parbatipur"));
        rangpurDistricts.put("Nilphamari", Arrays.asList("Nilphamari Sadar", "Dimla", "Domar", "Jaldhaka", "Kishoreganj", "Saidpur"));
        rangpurDistricts.put("Gaibandha", Arrays.asList("Gaibandha Sadar", "Fulchhari", "Gabtali", "Gobindaganj", "Palashbari", "Sadullapur", "Sundarganj"));
        rangpurDistricts.put("Kurigram", Arrays.asList("Kurigram Sadar", "Bhurungamari", "Char Rajibpur", "Chilmari", "Phulbari", "Nageshwari", "Rajarhat", "Raomari", "Ulipur"));
        rangpurDistricts.put("Lalmonirhat", Arrays.asList("Lalmonirhat Sadar", "Aditmari", "Hatibandha", "Kaliganj", "Patgram"));
        rangpurDistricts.put("Panchagarh", Arrays.asList("Panchagarh Sadar", "Atwari", "Boda", "Debiganj", "Tetulia"));
        rangpurDistricts.put("Thakurgaon", Arrays.asList("Thakurgaon Sadar", "Baliadangi", "Haripur", "Pirganj", "Ranisankail"));
        locationData.put("Rangpur", rangpurDistricts);
        
        // Mymensingh Division
        LinkedHashMap<String, List<String>> mymensinghDistricts = new LinkedHashMap<>();
        mymensinghDistricts.put("Mymensingh", Arrays.asList("Mymensingh Sadar", "Bhaluka", "Dhobaura", "Fulbaria", "Gaffargaon", "Gauripur", "Haluaghat", "Ishwarganj", "Muktagachha", "Nandail", "Phulpur", "Trishal", "Tara Khanda"));
        mymensinghDistricts.put("Jamalpur", Arrays.asList("Jamalpur Sadar", "Baksiganj", "Dewanganj", "Islampur", "Madarganj", "Melandaha", "Sarishabari"));
        mymensinghDistricts.put("Netrokona", Arrays.asList("Netrokona Sadar", "Atpara", "Barhatta", "Durgapur", "Kalmakanda", "Kendua", "Khaliajuri", "Madan", "Mohanganj", "Purbadhala"));
        mymensinghDistricts.put("Sherpur", Arrays.asList("Sherpur Sadar", "Jhenaigati", "Nakla", "Nalitabari", "Sreebardi"));
        locationData.put("Mymensingh", mymensinghDistricts);
        
        return locationData;
    }
    
    public static List<String> getDivisions() {
        return Arrays.asList("Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh");
    }
    
    public static List<String> getDistricts(String division) {
        LinkedHashMap<String, LinkedHashMap<String, List<String>>> locationData = getLocationData();
        LinkedHashMap<String, List<String>> districts = locationData.get(division);
        if (districts != null) {
            return Arrays.asList(districts.keySet().toArray(new String[0]));
        }
        return Arrays.asList();
    }
    
    public static List<String> getThanas(String division, String district) {
        LinkedHashMap<String, LinkedHashMap<String, List<String>>> locationData = getLocationData();
        LinkedHashMap<String, List<String>> districts = locationData.get(division);
        if (districts != null) {
            List<String> thanas = districts.get(district);
            if (thanas != null) {
                return thanas;
            }
        }
        return Arrays.asList();
    }
}
