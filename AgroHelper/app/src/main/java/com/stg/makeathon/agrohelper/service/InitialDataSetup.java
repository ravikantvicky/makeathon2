package com.stg.makeathon.agrohelper.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stg.makeathon.agrohelper.config.AppConstants;
import com.stg.makeathon.agrohelper.domain.Disease;

import java.lang.reflect.Type;
import java.util.List;

public class InitialDataSetup {
    public static void saveInitialData() {
        String jsonData = "[\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"name\": \"Apple Scab\",\n" +
                "\t  \"type\": \"Apple\",\n" +
                "      \"description\": \"A serious disease of apples, apple scab (Venturia inaequalis) attacks both leaves and fruit. Symptoms on fruit are similar to those found on leaves. The fungal disease forms dull black or grey-brown spots on the upper surface of leaves. Dark, velvety spots may appear on the lower surface. Scabby spots are sunken and tan and may have velvety spores in the center. Infected fruit becomes distorted and may crack allowing entry of secondary organisms. Severely affected fruit may drop, especially when young.\",\n" +
                "      \"cause\": \"Apple scab overwinters primarily in fallen leaves and in the soil. Disease development is favored by wet, cool weather that generally occurs in spring and early summer. Fungal spores are carried by wind, rain or splashing water from the ground to flowers, leaves or fruit. During damp or rainy periods, newly opening apple leaves are extremely susceptible to infection. The longer the leaves remain wet, the more severe the infection will be. Apple scab spreads rapidly between 55-75 degrees F.\",\n" +
                "      \"seasons\": \"Spring and Early Summer\",\n" +
                "      \"treatment\": \"1. Choose resistant varieties when possible.\\n2. Rake under trees and destroy infected leaves to reduce the number of fungal spores available to start the disease cycle over again next spring.\\n3. Water in the evening or early morning hours (avoid overhead irrigation) to give the leaves time to dry out before infection can occur.\\n4. Spread a 3-inch to 6-inch layer of compost under trees, keeping it away from the trunk, to cover soil and prevent splash dispersal of the fungal spores.\\n5. For best control, spray liquid copper soap early, two weeks before symptoms normally appear. Alternatively, begin applications when disease first appears, and repeat at 7 to 10 day intervals up to blossom drop.\\n6. Bonide Sulfur Plant Fungicide, a finely ground wettable powder, is used in pre-blossom applications and must go on before rainy or spore discharge periods. Apply from pre-pink through cover (2 Tbsp/ gallon of water), or use in cover sprays up to the day of harvest.\\n7. Organocide® Plant Doctor is an earth-friendly systemic fungicide that works its way through the entire plant to combat a large number of diseases on ornamentals, turf, fruit and more. Apply as a soil drench or foliar spray (3-4 tsp/ gallon of water) to prevent and attack fungal problems.\\n8. Containing sulfur and pyrethrins, Bonide® Orchard Spray is a safe, one-hit concentrate for insect attacks and fungal problems. For best results, apply as a protective spray (2.5 oz/ gallon) early in the season. If disease, insects or wet weather are present, mix 5 oz in one gallon of water. Thoroughly spray all parts of the plant, especially new shoots.\",\n" +
                "      \"moreDetails\": \"https://www.planetnatural.com/pest-problem-solver/plant-disease/apple-scab/\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"name\": \"Apple Black Rot\",\n" +
                "\t  \"type\": \"Apple\",\n" +
                "      \"description\": \"Black rot in apples is a common fungal disease that can spread from infected apple trees to other landscape plants, so it’s important to watch your apple trees for signs of black rot disease in order to catch it early in the disease cycle. Black rot infects fruit, leaves and bark caused by the fungus Botryosphaeria obtusa.\",\n" +
                "      \"cause\": \"Black rot propagates through spores over winter in the dead and diseased parts of the plant. Lack of sunlight and dampness. Spreads through dead or weak tissues and plants.\",\n" +
                "      \"seasons\": \"Winter\",\n" +
                "      \"treatment\": \"1. Weed regularly to improve air circulation and light access around all plants affected by black rot. \\n2. Remove all dead plant material from the area, placing it into plastic trash bags to prevent further spread of the disease.\\n3. Prune the dead or infected branches from apple trees, disinfecting the pruning shears with a 70 percent alcohol solution after each cut. \\n4. Also improve circulation in the center of the plant by pruning away congested branches.\\n5. Pluck all mummified fruit from apple trees as soon as possible, as these house fungal spores and cause the disease to spread. \\n6. Rotate crucifer crops every two years, using black rot-resistant cultivars. When replanting, soak seeds in hot water to kill black rot and wait until a warm, dry day to sow the seeds.\\n7. Treat stubborn grapevine and apple infections with a commercial fungicide containing captan once the year's new growth reaches 4 to 6 inches in length. Add 1 tablespoon of captan fungicide to a gallon of water and mix it thoroughly in a hand sprayer tank. Pumping the handle, coat the branches and leaves completely. Repeat the application once every 7 to 10 days until the fruit begins to change color.\\n8. Although general purpose fungicides, like copper-based sprays and lime sulfur, can be used to control black rot, nothing will improve apple black rot like removing all sources of spores.\",\n" +
                "      \"moreDetails\": \"https://homeguides.sfgate.com/treat-black-rot-80130.html\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 3,\n" +
                "      \"name\": \"Cedar apple rust\",\n" +
                "\t  \"type\": \"Apple\",\n" +
                "      \"description\": \"Cedar apple rust (Gymnosporangium juniperi-virginianae) is a fungal disease. On apple trees, look for pale yellow pinhead sized spots on the upper surface of the leaves shortly after bloom. These gradually enlarge to bright orange-yellow spots which make the disease easy to identify. Orange spots may develop on the fruit as well. Heavily infected leaves may drop prematurely.\",\n" +
                "      \"cause\": \"This disease that requires juniper plants to complete its complicated two year life-cycle. From year to year, the disease must pass from junipers to apples to junipers again; it cannot spread between apple trees. Spores overwinter as a reddish-brown gall on young twigs of various juniper species. In early spring, during wet weather, these galls swell and bright orange masses of spores are blown by the wind where they infect susceptible apple and crab-apple trees.\",\n" +
                "      \"seasons\": \"Early Spring, Winter and wet weather\",\n" +
                "      \"treatment\": \"1. Choose resistant cultivars when available.\\n2. Rake up and dispose of fallen leaves and other debris from under trees.\\n3. Remove galls from infected junipers. In some cases, juniper plants should be removed entirely.\\n4. Apply preventative, disease-fighting fungicides labeled for use on apples weekly, starting with bud break, to protect trees from spores being released by the juniper host. This occurs only once per year, so additional applications after this springtime spread are not necessary.\\n5. On juniper, rust can be controlled by spraying plants with a copper solution (0.5 to 2.0 oz/ gallon of water) at least four times between late August and late October.\\n6. Safely treat most fungal and bacterial diseases with SERENADE Garden. This broad spectrum bio-fungicide uses a patented strain of Bacillus subtilis that is registered for organic use. Best of all, SERENADE is completely non-toxic to honey bees and beneficial insects.\\n7. Containing sulfur and pyrethrins, Bonide Orchard Spray is a safe, one-hit concentrate for insect attacks and fungal problems. For best results, apply as a protective spray (2.5 oz/ gallon) early in the season. If disease, insects or wet weather are present, mix 5 oz in one gallon of water. Thoroughly spray all parts of the plant, especially new shoots.\",\n" +
                "      \"moreDetails\": \"https://www.planetnatural.com/pest-problem-solver/plant-disease/cedar-apple-rust/\"\n" +
                "    },\n" +
                "\t{\n" +
                "      \"id\": 4,\n" +
                "      \"name\": \"Healthy\",\n" +
                "\t  \"type\": \"Apple\",\n" +
                "      \"description\": \"Healthy Apple.\",\n" +
                "      \"cause\": \"\",\n" +
                "      \"seasons\": \"\",\n" +
                "      \"treatment\": \"\",\n" +
                "      \"moreDetails\": \"\"\n" +
                "    },\n" +
                "\t{\n" +
                "      \"id\": 5,\n" +
                "      \"name\": \"Healthy\",\n" +
                "\t  \"type\": \"Blueberry\",\n" +
                "      \"description\": \"Healthy Blueberry.\",\n" +
                "      \"cause\": \"\",\n" +
                "      \"seasons\": \"\",\n" +
                "      \"treatment\": \"\",\n" +
                "      \"moreDetails\": \"\"\n" +
                "    },\n" +
                "\t{\n" +
                "      \"id\": 6,\n" +
                "      \"name\": \"Healthy\",\n" +
                "\t  \"type\": \"Blueberry\",\n" +
                "      \"description\": \"Healthy Blueberry.\",\n" +
                "      \"cause\": \"\",\n" +
                "      \"seasons\": \"\",\n" +
                "      \"treatment\": \"\",\n" +
                "      \"moreDetails\": \"\"\n" +
                "    }\n" +
                "  ]";

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Disease>>(){}.getType();
        List<Disease> allDisease = gson.fromJson(jsonData, listType);
        if (allDisease != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference colRef = db.collection(AppConstants.FB_DISEASE_COLLECTION_NAME);
            for (Disease d: allDisease) {
                colRef.add(d);
            }
        }
    }
}
