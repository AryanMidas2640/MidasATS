package com.midas.consulting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailParser {

    static     String htmlContent = "<table style=\"border-collapse:collapse; width:100%\">\n" +
                " <tbody>\n" +
                "  <tr>\n" +
                "   <td colspan=\"4\" style=\"border:1px solid #dddddd; text-align:center; padding:8px; font-size:16px\"><strong>Requisition Details</strong> </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"width:15%; border:1px solid #dddddd; text-align:left; padding:8px\"><strong>State</strong></td>\n" +
                "   <td style=\"width:55%; border:1px solid #dddddd; text-align:left; padding:8px\"><span>Georgia</span></td>\n" +
                "   <td style=\"width:15%; border:1px solid #dddddd; text-align:left; padding:8px\"><strong>City</strong></td>\n" +
                "   <td style=\"width:15%; border:1px solid #dddddd; text-align:left; padding:8px\"><span>Lawrenceville</span></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Facility </strong></td>\n" +
                "   <td colspan=\"3\" style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>Northside Hospital Gwinnett</span></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Job Title(s)</strong> </td>\n" +
                "   <td colspan=\"3\" style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>CT Technologist (Allied)</span> \n" +
                "    <div></div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Requisition </strong></td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>2024-67814</span></td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Specialty/Dept.</strong></td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>Gwi-CT SCAN GMC (7290)</span></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Contract Start Date </strong></td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>Aug 05, 2024</span> \n" +
                "    <div>\n" +
                "     <span style=\"color:red; text-decoration:line-through\">\n" +
                "      <div style=\"white-space:pre-line\">\n" +
                "       Jul 29, 2024\n" +
                "      </div></span>\n" +
                "    </div></td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Contract End Date</strong> </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>Nov 02, 2024 </span>\n" +
                "    <div>\n" +
                "     <span style=\"color:red; text-decoration:line-through\">\n" +
                "      <div style=\"white-space:pre-line\">\n" +
                "       Oct 26, 2024\n" +
                "      </div></span>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td colspan=\"2\"></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Shift Type</strong> </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>10hr Nights</span> \n" +
                "    <div></div></td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Type</strong> </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>Travel </span></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Shift Duration (Hrs)</strong> </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>10 </span>\n" +
                "    <div></div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>OnCall</strong> </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span style=\"text-transform:capitalize\">no</span> \n" +
                "    <div></div></td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>OnCall Ratio</strong> </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span></span>\n" +
                "    <div></div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td colspan=\"4\" style=\"border:1px solid #dddddd; text-align:left; padding:8px\">\n" +
                "    <div>\n" +
                "     <strong>Call Specifics</strong>\n" +
                "    </div>\n" +
                "    <div style=\"white-space:pre-line\"></div>\n" +
                "    <div></div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Guaranteed Hours (Weekly)</strong> </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>40</span> \n" +
                "    <div></div></td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Weeks</strong> </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><span>13</span> \n" +
                "    <div></div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\"><strong>Selected Contract Type</strong> </td>\n" +
                "   <td colspan=\"3\" style=\"border:1px solid #dddddd; text-align:left; padding:8px\">Daily OT after <strong>24</strong> hours [Based on multiplier]<br><span>Weekly OT after <strong>40</strong> hours</span> [Based on multiplier] \n" +
                "    <div></div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td colspan=\"4\" style=\"border:1px solid #dddddd; text-align:left; padding:8px\">\n" +
                "    <div>\n" +
                "     <strong>Description / Comment</strong>\n" +
                "    </div>\n" +
                "    <div style=\"white-space:pre-line\">\n" +
                "     No local candidates within 50 miles. Schedule 730pm-6am 13 weeks 7290V CT Scan GMC Min of 2 years of experience within specialty. Required BLS. AART - CT Level 2 trauma center with proficiency in trauma stroke, cardiovascular, procedural and routine exams for all. Types of patients from emergency, inpatient and outpatient services. Operates computerized tomography equipment to produce films of designated portions of the human body for neonate, pediatric, adolescent, adult and geriatric patients. \n" +
                "    </div>\n" +
                "    <div></div></td>\n" +
                "  </tr>\n" +
                " </tbody>\n" +
                "</table>\n" +
                "<table style=\"border-collapse:collapse; width:60%\">\n" +
                " <tbody>\n" +
                "  <tr>\n" +
                "   <td colspan=\"2\" style=\"border:1px solid #dddddd; text-align:center; padding:8px; font-size:16px\"><strong>Rates for CT Technologist </strong></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px; width:40%\"><strong>Shift Type </strong></td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:center; padding:8px; width:30%\"><strong>Requisition Override Rates</strong></td>\n" +
                "  </tr>\n" +
                " </tbody>\n" +
                " <tbody>\n" +
                "  <tr>\n" +
                "   <td colspan=\"2\" style=\"border:1px solid #dddddd; text-align:left; padding:8px; background-color:rgba(0,0,0,0.07); font-weight:bold; padding:5px 5px\"><strong>Hourly</strong> </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">10hr Day </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">10hr Eve </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">10hr Nights </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">12Hr Eve </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">12hr Days </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">12hr Nights </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">8hr Days </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">8hr Days (Orientation) </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">8hr Eve </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">8hr Nights </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">8hr Nights (Orientation) </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $100.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">8hr Days (Holiday) </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:55px; background:#f4f4f4\">\n" +
                "      <div style=\"border-bottom:1px dotted #ccc; padding-top:5px; padding-bottom:5px; min-height:15px\">\n" +
                "       1.25\n" +
                "      </div>\n" +
                "      <div style=\"margin-top:5px\">\n" +
                "       Multi <span title=\"Base: $100.00\">$</span> \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:55px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:18px\">\n" +
                "       $125.00\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">Charge Shift </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:55px; background:#f4f4f4\">\n" +
                "      <div style=\"border-bottom:1px dotted #ccc; padding-top:5px; padding-bottom:5px; min-height:15px\">\n" +
                "       2.00\n" +
                "      </div>\n" +
                "      <div style=\"margin-top:5px\">\n" +
                "       Diff <span title=\"Base: $100.00\">$</span> \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:55px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:18px\">\n" +
                "       $102.00\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td colspan=\"2\" style=\"border:1px solid #dddddd; text-align:left; padding:8px; background-color:rgba(0,0,0,0.07); font-weight:bold; padding:5px 5px\"><strong>Oncall</strong> </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">24hr On-Call </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:30px; background:#f4f4f4\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       Fixed\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:30px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:3px\">\n" +
                "       $5.00 \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td colspan=\"2\" style=\"border:1px solid #dddddd; text-align:left; padding:8px; background-color:rgba(0,0,0,0.07); font-weight:bold; padding:5px 5px\"><strong>Callback</strong> </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">Callback | 24hr On-Call </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:55px; background:#f4f4f4\">\n" +
                "      <div style=\"border-bottom:1px dotted #ccc; padding-top:5px; padding-bottom:5px; min-height:15px\">\n" +
                "       1.00\n" +
                "      </div>\n" +
                "      <div style=\"margin-top:5px\">\n" +
                "       Multi <span title=\"Base: $100.00\">$</span> \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:55px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:18px\">\n" +
                "       $100.00\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td colspan=\"2\" style=\"border:1px solid #dddddd; text-align:left; padding:8px; background-color:rgba(0,0,0,0.07); font-weight:bold; padding:5px 5px\"><strong>OT</strong> </td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">Daily OT </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:55px; background:#f4f4f4\">\n" +
                "      <div style=\"border-bottom:1px dotted #ccc; padding-top:5px; padding-bottom:5px; min-height:15px\">\n" +
                "       1.25\n" +
                "      </div>\n" +
                "      <div style=\"margin-top:5px\">\n" +
                "       Multi <span title=\"Base: $100.00\">$</span> \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:55px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:18px\">\n" +
                "       $125.00\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:left; padding:8px\">Weekly OT (40) </td>\n" +
                "   <td style=\"border:1px solid #dddddd; text-align:right; padding:8px\">\n" +
                "    <div style=\"width:180px; border:1px solid #e6e6e6; border-radius:2px; text-align:right; color:#444; margin:auto\">\n" +
                "     <div style=\"width:45%; box-sizing:border-box; padding:2px; padding-right:5px; float:left; min-height:55px; background:#f4f4f4\">\n" +
                "      <div style=\"border-bottom:1px dotted #ccc; padding-top:5px; padding-bottom:5px; min-height:15px\">\n" +
                "       1.25\n" +
                "      </div>\n" +
                "      <div style=\"margin-top:5px\">\n" +
                "       Multi <span title=\"Base: $100.00\">$</span> \n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"width:55%; box-sizing:border-box; padding:2px; min-height:55px; padding-left:5px; float:right; border-left:1px solid #e6e6e6; background:#fff\">\n" +
                "      <div style=\"padding-top:18px\">\n" +
                "       $125.00\n" +
                "      </div>\n" +
                "     </div>\n" +
                "     <div style=\"clear:both\"></div>\n" +
                "    </div></td>\n" +
                "  </tr>\n" +
                " </tbody>\n" +
                "</table>";

    public static Map<String, String> parseEmail(String emailContent) throws Exception {
        Map<String, String> requisitionDetails = new HashMap<>();
        Document doc = Jsoup.parse(emailContent);

        // Find the table with requisition details
        List<Element> requisitionTable = doc.select("table").first().select("tr");

        // Skip the header row
        requisitionTable = requisitionTable.subList(1, requisitionTable.size());

        for (Element row : requisitionTable) {
            Elements cols = row.select("td");
            if (cols.size() < 4) {
                continue;
            }
            String col1Value = cols.get(0).text().trim();
            String col4Value = cols.get(3).text().trim();
            // Extract value from specific elements within cells (optional)
            Elements col1ValueElements = cols.get(0).select("*");
            if (!col1ValueElements.isEmpty()) {
                col1Value = col1ValueElements.get(0).text().trim();
            }
            Elements col4ValueElements = cols.get(3).select("*");
            if (!col4ValueElements.isEmpty()) {
                col4Value = col4ValueElements.get(0).text().trim();
            }
            // Store key-value pairs (using a String as a temporary key)
            requisitionDetails.put(col1Value + "_" + col4Value, col1Value + "," + col4Value);
        }

        return requisitionDetails;
    }

    public static void main(String[] args) throws Exception {
        String emailContent = htmlContent; //"... your email content here ..."; // Replace with actual email content
        Map<String, String> details = parseEmail(emailContent);

        for (Map.Entry<String, String> entry : details.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
//        // HashMaps to store parsed data
//        HashMap<String, String> requisitionDetails = new HashMap<>();
////        HashMap<String, String> ratesForCTTechnologist = new HashMap<>();
//        Document doc = Jsoup.parse(htmlContent);
//
//        // Parsing Requisition Details
//        Element requisitionDetailsTable = doc.select("table").get(0); // Assuming the first table is the requisition details
//        Elements rows = requisitionDetailsTable.select("tr");
//
//        for (Element row : rows) {
//            Elements columns = row.select("td");
//            if (columns.size() > 1) {
//                String key = columns.get(0).text();
//                String value = columns.get(1).select("s").isEmpty() ? columns.get(1).text() : "";
//                requisitionDetails.put(key, value);
//            }
//        }
//
//        // Parsing Rates for CT Technologist
//        Element ratesTable = doc.select("table").get(1); // Assuming the second table is the rates table
//        Elements rateRows = ratesTable.select("tr");
//
//        for (Element row : rateRows) {
//            Elements columns = row.select("td");
//            if (columns.size() > 1) {
//                String key = columns.get(0).text();
//                String value = columns.get(1).select("s").isEmpty() ? columns.get(1).text() : "";
//                requisitionDetails.put(key, value);
//            }
//        }
//
//        // Printing the parsed data
//        System.out.println("Requisition Details:");
//        for (String key : requisitionDetails.keySet()) {
//            System.out.println(key + " : " + requisitionDetails.get(key));
//        }

//        System.out.println("\nRates for CT Technologist:");
//        for (String key : ratesForCTTechnologist.keySet()) {
//            System.out.println(key + " : " + ratesForCTTechnologist.get(key));
//        }

}